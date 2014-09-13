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

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

import com.google.common.base.Joiner;

import eu.skqs.type.Dynasty;
import eu.skqs.type.PersName;
import eu.skqs.type.Measure;


public class DateTimeAnalysisEngine extends JCasAnnotator_ImplBase {

	// Dynasties
	private String	mDynastiesBase;

	// Dynasties patterns
	private Pattern	mDynastiesPattern;
	private Pattern	mDynastiesPrefixPattern;

	private HashMap<String, String> mDynasties;

	// Temporal markers
	private Pattern	mTemporalPattern = Pattern.compile("以前|以後");

	// Logger
	private Logger	logger;

	// Annotation
	private int totalDynasties = 0;

	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);

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
		mDynasties.put("遼", "XXX");
		mDynasties.put("金", "XXX");
		mDynasties.put("元", "XXX");
		mDynasties.put("明", "XXX");
		mDynasties.put("清", "XXX");
		mDynasties.put("國朝", "XXX");

		mDynastiesBase = "(" + Joiner.on("|").join(mDynasties.keySet()) + ")";


		// Dynasty + Expression
		mDynastiesPattern = Pattern.compile(mDynastiesBase + "(興|以後|以來)");

		// Dynasty prefix
		mDynastiesPrefixPattern = Pattern.compile(".*" + mDynastiesBase + "$");

		logger = getContext().getLogger();
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		// Get document text
		String docText = aJCas.getDocumentText();

		int documentLength = docText.length();
		int pos = 0;

		// Dynasties expressions
		Matcher matcher = mDynastiesPattern.matcher(docText);
		while (matcher.find(pos)) {

			// Found match
			Dynasty annotation = new Dynasty(aJCas, matcher.start(1), matcher.end(1));

			annotation.addToIndexes();

			totalDynasties++;

			logger.log(Level.WARNING, "Found: " + annotation);

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
			if (documentLength > localBegin) {
				// TODO: substring needs only to be 1 or 2 characters
				matcher = mDynastiesPrefixPattern.matcher(
				    docText.substring(0, localBegin));

				if (matcher.matches()) {
					Dynasty dynasty = new Dynasty(aJCas,
					    matcher.start(1), matcher.end(1));
					dynasty.addToIndexes();
				}
			}
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		System.out.println("Total dynasties: " + totalDynasties);
	}
}
