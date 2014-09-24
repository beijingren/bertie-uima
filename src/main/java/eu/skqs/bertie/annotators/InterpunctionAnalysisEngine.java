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
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

import com.google.common.base.Joiner;

import eu.skqs.type.Pc;


public class InterpunctionAnalysisEngine extends JCasAnnotator_ImplBase {

	// Interpuction
	private Pattern mInterpunctionPattern;

	// Logger
	private Logger logger;

	// Annotation
	private int totalInterpunction = 0;

	@Override
	public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
		super.initialize(uimaContext);

		logger = uimaContext.getLogger();
		logger.log(Level.FINE, "InterpunctionAnalysisEngine initialize...");

		mInterpunctionPattern = Pattern.compile("，|。|！|、|“|”|「|」|：|？|《|》|•|（|）|；");
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		logger.log(Level.FINE, "InterpunctionAnalysisEngine process...");

		// Get document text
		String docText = aJCas.getDocumentText();

		int pos = 0;

		// Interpuction
		Matcher matcher = mInterpunctionPattern.matcher(docText);
		while (matcher.find(pos)) {

			// Found match
			Pc annotation = new Pc(aJCas, matcher.start(), matcher.end());

			annotation.addToIndexes();

			totalInterpunction++;

			logger.log(Level.FINEST, "Found: " + annotation);

			pos = matcher.end();
		}

	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		logger.log(Level.FINE, "Total interpunction: " + totalInterpunction);
	}
}
