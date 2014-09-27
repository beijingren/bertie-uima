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
import java.util.Vector;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

import com.google.common.base.Joiner;

import eu.skqs.bertie.resources.PlaceNameResource;
import eu.skqs.type.PlaceName;


public class PlaceNameAnalysisEngine extends JCasAnnotator_ImplBase {

	// Logger
	private Logger logger;

	// Shared resources
	public final static String MODEL_KEY = "PlaceNameResource";
	@ExternalResource(key = MODEL_KEY)
	private PlaceNameResource placeNameResource;

	// Patterns
	private Pattern mPlaceNamePattern;

	private Vector mPlaceNameVector;

	// Annotation count
	private int totalPlaceNames = 0;

	@Override
	public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
		super.initialize(uimaContext);

		// Logger
		logger = uimaContext.getLogger();
		logger.log(Level.FINE, "PlaceNameAnalysisEngine initialize...");

		// Get place names from shared resource
		mPlaceNameVector = placeNameResource.getPlaceNames();
		mPlaceNamePattern = Pattern.compile(Joiner.on("|").join(
		    mPlaceNameVector));
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		logger.log(Level.FINE, "PlaceNameAnalysisEngine process...");

		// Get document text
		String docText = aJCas.getDocumentText();

		int pos = 0;
		Matcher matcher = null;

		// Annotate place names
		if (mPlaceNameVector.isEmpty()) {
			logger.log(Level.WARNING, "   mPlaceNameVector is empty.");
		} else {
			pos = 0;
			matcher = mPlaceNamePattern.matcher(docText);
			while (matcher.find(pos)) {

				// Found match
				PlaceName annotation = new PlaceName(aJCas, matcher.start(), matcher.end());

				annotation.addToIndexes();

				totalPlaceNames++;

				pos = matcher.end();
			}
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		logger.log(Level.FINE, "Total place names: " + totalPlaceNames);
	}
}
