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
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

import eu.skqs.type.Div;
import eu.skqs.type.P;


public class PreprocessPlainAnalysisEngine extends JCasAnnotator_ImplBase {

	// Logger
	private Logger logger;

	// Config parameter
	public static final String PARAM_MODE = "AnalysisMode";

	// Patterns
	private Pattern mParagraphPattern;

	// Params
	private String mMode;

	public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
		super.initialize(uimaContext);

		logger = uimaContext.getLogger();
		logger.log(Level.FINE, "PreprocessPlainAnalysisEngine initialize...");

		mMode = ((String) uimaContext.getConfigParameterValue(PARAM_MODE)).trim();

		mParagraphPattern = Pattern.compile("(^.*\\S+.*$)+", Pattern.MULTILINE);
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		logger.log(Level.FINE, "PreprocessPlainAnalysisEngine process...");

		String documentText = jcas.getDocumentText();

		if (mMode.equals("poetry")) {
			System.out.println("XXXXXX poetry");
		}

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
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
	}
}
