/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package eu.skqs.bertie.annotators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.apache.uima.jcas.tcas.Annotation;

import com.google.common.base.Joiner;

import eu.skqs.bertie.resources.SPARQLSharedResource;
import eu.skqs.bertie.util.AnnotationRetrieval;
import eu.skqs.type.PersName;
import eu.skqs.type.Measure;
import eu.skqs.type.Date;
import eu.skqs.type.Num;
import eu.skqs.type.Time;

// TODO: move sparql code into resource sharing code
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.util.Vector;


public class DateTimeAnalysisEngine extends JCasAnnotator_ImplBase {

	// Logger
	private Logger logger;

	// Shared resources
	public final static String MODEL_KEY = "SPARQLSharedResource";
	@ExternalResource(key = MODEL_KEY)
	private SPARQLSharedResource sparqlSharedResource;

	// Dynasties
	private String	mDynastiesBase;
	private String	mTwoCharacterDynasties;

	// Dynasties patterns
	private Pattern	mDynastiesPattern;
	private Pattern	mDynastiesExpressionPattern;
	private Pattern	mDynastiesPrefixPattern;
	private Pattern	mSexagenaryCyclePattern;
	private Pattern	mEraNamePattern;
	private Pattern	mTwoCharacterDynastiesPattern;

	private HashMap<String, String> mDynasties;
	private Map<String, String> mTimeExpressionsMap;
	private Map<String, Integer> mSexagenaryCycleMap;
	private Map<String, Map<String, Integer>> mEraNameMap;

	// Temporal markers
	private Pattern	mTemporalPattern = Pattern.compile("以前|以後");
	private Pattern	mTimeExpressionsPattern;
	private Pattern	mEmperorTimeExpressionPattern;

	// Annotation
	private int totalDynasties = 0;

	@Override
	public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
		super.initialize(uimaContext);

		logger = uimaContext.getLogger();
		logger.log(Level.FINE, "DateTimeAnalysisEngine initialize...");

		mTimeExpressionsMap = new HashMap<String, String>();
		mTimeExpressionsMap.put("夜半", "24:00");

		mTimeExpressionsPattern = Pattern.compile("(" + Joiner.on("|").join(mTimeExpressionsMap.keySet()) + ")");

		// TODO: only match 2 character phrases
		// single character phrase only before person names
		mDynasties = new HashMap<String, String>();
		mDynasties.put("三國", "XXX");
		mDynasties.put("隋", "XXX");
		mDynasties.put("唐", "XXX");
		mDynasties.put("五代", "XXX");
		mDynasties.put("後蜀", "XXX");
		mDynasties.put("吳", "XXX");
		mDynasties.put("南唐", "XXX");
		mDynasties.put("吳越", "XXX");
		mDynasties.put("宋", "XXX");
		mDynasties.put("南宋", "XXX");
// Bei song
		mDynasties.put("遼", "XXX");
		mDynasties.put("金", "XXX");
		mDynasties.put("元", "XXX");
		mDynasties.put("明", "XXX");
		mDynasties.put("清", "XXX");
		mDynasties.put("國朝", "XXX");

		mDynastiesBase = "(" + Joiner.on("|").join(mDynasties.keySet()) + ")";
		mTwoCharacterDynasties = "(三國|五代|後蜀|南唐|吳越|南宋|國朝)";

		mDynastiesPattern = Pattern.compile(mDynastiesBase);
		mTwoCharacterDynastiesPattern = Pattern.compile(mTwoCharacterDynasties);

		// Dynasty + Expression
		mDynastiesExpressionPattern = Pattern.compile(mDynastiesBase + "(興|以後|以來)");

		// Dynasty prefix
		mDynastiesPrefixPattern = Pattern.compile(".*" + mDynastiesBase + "$");

		// 日食

		mSexagenaryCycleMap = SPARQLSharedResource.getSexagenaryCycles();
		mSexagenaryCyclePattern = Pattern.compile(Joiner.on("|").join(
		    mSexagenaryCycleMap.keySet()));

		mEraNameMap = SPARQLSharedResource.getEraNames();
		mEraNamePattern = Pattern.compile(Joiner.on("|").join(
		    mEraNameMap.keySet()));
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		logger.log(Level.FINE, "DateTimeAnalysisEngine process...");

		// Get document text
		String docText = aJCas.getDocumentText();
		int documentLength = docText.length();

		int pos = 0;
		Matcher matcher = null;

		// Sexagenary cycle
		if (mSexagenaryCycleMap.isEmpty()) {
			logger.log(Level.WARNING, "   SexagenaryCycleMap is empty.");
		} else {
			pos = 0;
			matcher = mSexagenaryCyclePattern.matcher(docText);
			while (matcher.find(pos)) {
				Num annotation = new Num(aJCas, matcher.start(), matcher.end());

				int value = mSexagenaryCycleMap.get(matcher.group());
				annotation.setValue(value);
				annotation.addToIndexes();

				pos = matcher.end();
			}
		}

		// Era names
		if (mEraNameMap.isEmpty()) {
			logger.log(Level.WARNING, "    EraNameMap is empty.");
		} else {
			pos = 0;
			matcher = mEraNamePattern.matcher(docText);
			while (matcher.find(pos)) {
				Date annotation = new Date(aJCas, matcher.start(), matcher.end());

				Map<String, Integer> eraMap = mEraNameMap.get(matcher.group());
				Integer notBefore = eraMap.get("beginYear");
				Integer notAfter = eraMap.get("endYear");

				annotation.setNotBefore(notBefore.toString());
				annotation.setNotAfter(notAfter.toString());
				annotation.addToIndexes();

				pos = matcher.end();
			}
		}

		// Era name + Year
		FSIndex measureIndex = aJCas.getAnnotationIndex(Measure.type);
		FSIterator measureIterator = measureIndex.iterator();
		while (measureIterator.hasNext()) {
			Annotation annotation = (Annotation)measureIterator.next();
			Measure measure = (Measure)annotation;

			String unit = measure.getUnit();
			if ("year".equals(unit) || "years".equals(unit)) {
				Annotation eraAnnotation = AnnotationRetrieval.getAdjacentAnnotation(aJCas,
				    annotation, Date.class, true);

				if (eraAnnotation != null) {
					Date era = (Date)eraAnnotation;

					// 歲 BUG
					Integer notBefore = new Integer(0);
					try {
						
						notBefore = Integer.parseInt(era.getNotBefore());
					} catch (NumberFormatException e) {
						logger.log(Level.WARNING, "SPECIAL DATE PATTERN FIX ME!!!");
						System.out.println(era.getCoveredText());
						System.out.println(era.getNotBefore());
						//e.printStackTrace();
						//System.exit(-1);
						continue;
					}

					Integer quantity = measure.getQuantity();
					Integer dateWhen = notBefore + quantity - 1;

					Date date = new Date(aJCas);
					date.setBegin(era.getBegin());
					date.setEnd(measure.getEnd());
					//date.setNotBefore(dateWhen);
					//date.setNotAfter(dateWhen);
					date.setWhen(dateWhen.toString());

					date.addToIndexes();
				}
			}
		}

		// Time expressions
		pos = 0;
		matcher = mTimeExpressionsPattern.matcher(docText);
		while (matcher.find(pos)) {
			String when = mTimeExpressionsMap.get(matcher.group());

			Time annotation = new Time(aJCas, matcher.start(1), matcher.end(1));
			annotation.setWhen(when);
			annotation.addToIndexes();

			pos = matcher.end();
		}

		// Dynasties expressions
		pos = 0;
		matcher = mDynastiesExpressionPattern.matcher(docText);
		while (matcher.find(pos)) {
			Date annotation = new Date(aJCas, matcher.start(1), matcher.end(1));

			annotation.setNotBefore("9999");
			annotation.setNotAfter("9999");
			annotation.addToIndexes();

			totalDynasties++;
			pos = matcher.end();
		}

		// Dynasties with two characters
		pos = 0;
		matcher = mTwoCharacterDynastiesPattern.matcher(docText);
		while (matcher.find(pos)) {
			Date annotation = new Date(aJCas, matcher.start(1), matcher.end(1));

			annotation.setNotBefore("9999");
			annotation.setNotAfter("9999");
			annotation.addToIndexes();

			totalDynasties++;
			pos = matcher.end();
		}

		// Temporal
		pos = 0;
		matcher = mTemporalPattern.matcher(docText);
		while (matcher.find(pos)) {


			pos = matcher.end();
		}

		// Dynasty + Person
		FSIndex personIndex = aJCas.getAnnotationIndex(PersName.type);
		FSIterator personIterator = personIndex.iterator();
		while (personIterator.hasNext()) {
			PersName person = (PersName)personIterator.next();

			int localBegin = person.getBegin();

			// Prefix
			String localPrefix = null;
			try {
				localPrefix = docText.substring(localBegin-1, localBegin);
			} catch (StringIndexOutOfBoundsException e) {
				continue;
			}

			matcher = mDynastiesPattern.matcher(localPrefix);
			if (matcher.matches()) {
				Date annotation = new Date(aJCas, localBegin-1, localBegin);
				annotation.addToIndexes();
			}
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		logger.log(Level.FINE, "Total dynasties: " + totalDynasties);
	}
}
