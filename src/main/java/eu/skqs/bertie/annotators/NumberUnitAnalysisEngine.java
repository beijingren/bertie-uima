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
	private Pattern mTimePostfixPattern;

	// Logger
	private Logger logger;

	// Annotation
	private int totalNumerals = 0;

	private String mNumeralsBase = "([一二三四五六七八九十百]+)";

	private Map<String, Integer> mNumeralsMap;
	private HashMap<String, Integer> mFixedTimeExpression;
	private Map<String, String> mTimePostfixMap;


	@Override
	public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
		super.initialize(uimaContext);

		logger = uimaContext.getLogger();
		logger.log(Level.INFO, "NumberUnitAnalysisEngine initialize...");

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
		mNumeralsMap.put("千", 1000);
		mNumeralsMap.put("萬", 10000);

		mNumeralsPattern = Pattern.compile("[一二三四五六七八九十百千萬]+", Pattern.MULTILINE);

		mTimePostfixMap = new HashMap<String, String>();
		mTimePostfixMap.put("歲", "year");
		mTimePostfixMap.put("年", "year");
		mTimePostfixMap.put("日", "day");
		mTimePostfixMap.put("月", "month");
		mTimePostfixMap.put("州", "prefecture");
		mTimePostfixMap.put("卷", "chapter");

		mTimePostfixPattern = Pattern.compile("(" + Joiner.on("|").join(mTimePostfixMap.keySet()) + ")", Pattern.UNICODE_CHARACTER_CLASS);

		mYearMeasurePattern = Pattern.compile(mNumeralsBase + "(歲|年)");

		mFixedTimeExpressionPattern = Pattern.compile(
		    Joiner.on("|").join(mFixedTimeExpression.keySet()));
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		logger.log(Level.INFO, "NumberUnitAnalysisEngine process...");

		String docText = jcas.getDocumentText();
		int documentLength = docText.length();

		int pos = 0;
		Matcher matcher = null;

		// Numerals
		pos = 0;
		matcher = mNumeralsPattern.matcher(docText);
		while (matcher.find(pos)) {
			Num annotation  = new Num(jcas, matcher.start(),
			    matcher.end());

			// Calculate value
			String numeral = matcher.group();
			int result = 0;
			for (int i = 0; i < numeral.length(); i++) {
				String singleNumeral = numeral.substring(i, i+1);
				int val = mNumeralsMap.get(singleNumeral);
				if (val < 10) {
					result = result + val;
				} else {
					// 十 , 斬首萬餘
					if (val >= 10 && result == 0) {
						result = val;
					} else {
						result = result * val;
					}
				}
			}
			annotation.setValue(result);
			annotation.addToIndexes();

			totalNumerals++;
			pos = matcher.end();
		}

		// Measures
		// TODO: rounded measures missing
		FSIndex numeralsIndex = jcas.getAnnotationIndex(Num.type);
		FSIterator numeralsIterator = numeralsIndex.iterator();
		while (numeralsIterator.hasNext()) {
			Num num = (Num)numeralsIterator.next();

			int localBegin = num.getBegin();
			int localEnd = num.getEnd();

			// Postfix
			String localPostfix = null;
			try {
				localPostfix = docText.substring(localEnd, localEnd+1);
			} catch (StringIndexOutOfBoundsException e) {
				// Ignore
			} finally {
				matcher = mTimePostfixPattern.matcher(localPostfix);
				if (matcher.matches()) {
					Measure annotation = new Measure(jcas, localBegin, localEnd+1);

					int quantity = num.getValue();
					String unit = mTimePostfixMap.get(matcher.group());
					if (quantity > 1) {
						unit += "s"; // TODO: language library
					}

					annotation.setQuantity(quantity);
					annotation.setUnit(unit);
					annotation.addToIndexes();
				}
			}
		}

		// Fixed expressions
		pos = 0;
		matcher = mFixedTimeExpressionPattern.matcher(docText);
		while (matcher.find(pos)) {
			Measure annotation = new Measure(jcas, matcher.start(), matcher.end());
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
