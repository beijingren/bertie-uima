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

import com.google.common.base.Joiner;

import eu.skqs.bertie.resources.SPARQLSharedResource;
import eu.skqs.type.Dynasty;
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

	// TODO:
	// RDF
	private String rdfFile = "/docker/dublin-store/rdf/sikuquanshu.rdf";
	private int prefixLength = "http://example.org/owl/sikuquanshu#".length();

	// Dynasties
	private String	mDynastiesBase;
	private String	mTwoCharacterDynasties;

	// Dynasties patterns
	private Pattern	mDynastiesPattern;
	private Pattern	mDynastiesExpressionPattern;
	private Pattern	mDynastiesPrefixPattern;
	private Pattern	mSexagenaryCyclePattern;
	private Pattern	mTwoCharacterDynastiesPattern;

	private HashMap<String, String> mDynasties;
	private Map<String, String> mTimeExpressionsMap;
	private Map<String, Integer> mSexagenaryCycleMap;

	// Temporal markers
	private Pattern	mTemporalPattern = Pattern.compile("以前|以後");
	private Pattern	mTimeExpressionsPattern;

	// Annotation
	private int totalDynasties = 0;

	@Override
	public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
		super.initialize(uimaContext);

		logger = uimaContext.getLogger();
		logger.log(Level.INFO, "DateTimeAnalysisEngine initialize...");

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
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		logger.log(Level.INFO, "DateTimeAnalysisEngine process...");

		// Get document text
		String docText = aJCas.getDocumentText();
		int documentLength = docText.length();

		int pos = 0;
		Matcher matcher = null;

		// Sexagenary cycle
		if (mSexagenaryCycleMap.isEmpty()) {
			logger.log(Level.WARNING, "SexagenaryCycleMap is empty.");
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
			annotation.addToIndexes();

			totalDynasties++;
			pos = matcher.end();
		}

		// Dynasties with two characters
		pos = 0;
		matcher = mTwoCharacterDynastiesPattern.matcher(docText);
		while (matcher.find(pos)) {
			Date annotation = new Date(aJCas, matcher.start(1), matcher.end(1));
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
		System.out.println("Total dynasties: " + totalDynasties);
	}
}
