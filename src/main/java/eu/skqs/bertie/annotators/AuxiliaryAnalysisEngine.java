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

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.jcas.JCas;

import eu.skqs.type.Div;
import eu.skqs.type.P;
import eu.skqs.type.Title;


public class AuxiliaryAnalysisEngine extends JCasAnnotator_ImplBase {
	private Pattern mParagraphPattern;
	private Pattern mTitlePattern;

	public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
		super.initialize(uimaContext);

		mParagraphPattern = Pattern.compile("(^.*\\S+.*$)+", Pattern.MULTILINE);

		mTitlePattern = Pattern.compile("《(.+)》", Pattern.MULTILINE);
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		String documentText = jcas.getDocumentText();

		// Paragraphs
		int pos = 0;
		Matcher matcher = mParagraphPattern.matcher(documentText);
		while (matcher.find(pos)) {

			Div div = new Div(jcas, matcher.start(), matcher.end());
			div.addToIndexes();

			P p = new P(jcas, matcher.start(), matcher.end());
			p.addToIndexes();

			pos = matcher.end();
		}

		// Titles
		pos = 0;
		matcher = mTitlePattern.matcher(documentText);
		while (matcher.find(pos)) {
			Title title = new Title(jcas, matcher.start(), matcher.end());
			title.addToIndexes();

			pos = matcher.end();
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
	}
}
