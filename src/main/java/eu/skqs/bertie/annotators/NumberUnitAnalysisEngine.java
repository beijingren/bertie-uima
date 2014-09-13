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
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

import com.google.common.base.Joiner;

import eu.skqs.type.Num;
import eu.skqs.type.Measure;


public class NumberUnitAnalysisEngine extends JCasAnnotator_ImplBase {

	// Measure patterns
	private Pattern mNumeralsPattern;
	private Pattern mYearMeasurePattern;
	private Pattern mFixedTimeExpressionPattern;

	// Logger
	private Logger logger;

	// Annotation
	private int totalNumerals = 0;

	private String mNumeralsBase = "([一二三四五六七八九十百]+)";

	private Map<String, Integer> mNumeralsMap;
	private HashMap<String, Integer> mFixedTimeExpression;


	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);

		mFixedTimeExpression = new HashMap<String, Integer>();
		mFixedTimeExpression.put("及冠", 20);

		mNumeralsMap = new HashMap<String, Integer>();
		mNumeralsMap.put("一", 1);
		mNumeralsMap.put("二", 2);
		mNumeralsMap.put("三", 3);
		mNumeralsMap.put("四", 4);
		mNumeralsMap.put("五", 5);
		mNumeralsMap.put("六", 6);
		mNumeralsMap.put("七", 7);
		mNumeralsMap.put("八", 8);
		mNumeralsMap.put("九", 9);
		mNumeralsMap.put("十", 10);
		mNumeralsMap.put("百", 100);

		mNumeralsPattern = Pattern.compile("[一二三四五六七八九十百]+");

		mYearMeasurePattern = Pattern.compile(mNumeralsBase + "(歲|年)");

		mFixedTimeExpressionPattern = Pattern.compile(
		    Joiner.on("|").join(mFixedTimeExpression.keySet()));

		logger = getContext().getLogger();
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		// Get document text
		String docText = aJCas.getDocumentText();

		int pos = 0;

		// Measures
		Matcher matcher = mYearMeasurePattern.matcher(docText);
		while (matcher.find(pos)) {

			// Found match
			Measure annotation = new Measure(aJCas, matcher.start(), matcher.end());

			annotation.setQuantity(2);
			annotation.setUnit("Year");
			annotation.addToIndexes();

			totalNumerals++;

			logger.log(Level.FINEST, "Found: " + annotation);

			pos = matcher.end();
		}

		// Fixed expressions
		pos = 0;
		matcher = mFixedTimeExpressionPattern.matcher(docText);
		while (matcher.find(pos)) {
			Measure annotation = new Measure(aJCas, matcher.start(), matcher.end());
			annotation.addToIndexes();
			System.out.println("Fixed time express");

			pos = matcher.end();
		}


	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		System.out.println("Total interpunction: " + totalNumerals);
	}
}
