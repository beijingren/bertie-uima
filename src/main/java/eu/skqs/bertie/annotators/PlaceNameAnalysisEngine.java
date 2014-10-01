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
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

import com.google.common.base.Joiner;

import eu.skqs.bertie.resources.PlaceNameResource;
import eu.skqs.type.PlaceName;
import eu.skqs.type.Measure;


public class PlaceNameAnalysisEngine extends JCasAnnotator_ImplBase {

	// Logger
	private Logger logger;

	// Shared resources
	public final static String MODEL_KEY = "PlaceNameResource";
	@ExternalResource(key = MODEL_KEY)
	private PlaceNameResource placeNameResource;

	// Patterns
	private Pattern mPlaceNamePattern;
	private Pattern mZhouEnumerationPattern;

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

		// Patterns
		mZhouEnumerationPattern = Pattern.compile("(\\p{Alnum}{1})、?", Pattern.UNICODE_CHARACTER_CLASS);
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		logger.log(Level.FINE, "PlaceNameAnalysisEngine process...");

		// Get document text
		String docText = jcas.getDocumentText();

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
				PlaceName annotation = new PlaceName(jcas, matcher.start(), matcher.end());

				annotation.addToIndexes();

				totalPlaceNames++;

				pos = matcher.end();
			}
		}

		// 蓬、閬、渠、達 四州
		FSIndex measureIndex = jcas.getAnnotationIndex(Measure.type);
		FSIterator measureIterator = measureIndex.iterator();
		while (measureIterator.hasNext()) {
			Measure measure = (Measure)measureIterator.next();

			String unit = measure.getUnit();
			if ("prefecture".equals(unit) || "prefectures".equals(unit)) {

				int localBegin = measure.getBegin();
				int prefixLength = measure.getQuantity();
				int localPos = 0;

				// Prefix
				String localPrefix = null;
				try {
					localPrefix = docText.substring(localBegin-prefixLength, localBegin);
				} catch (StringIndexOutOfBoundsException e) {
					continue;
				}

				// TODO:

				// Prefix with modern interpuction
				try {
					localPrefix = docText.substring(localBegin-2*(prefixLength-1)-1, localBegin);
				} catch (StringIndexOutOfBoundsException e) {
					continue;
				}

				matcher = mZhouEnumerationPattern.matcher(localPrefix);
				while (matcher.find(localPos)) {
					String zhouString = matcher.group(1) + "州";

					if (mPlaceNameVector.contains(zhouString)) {
						PlaceName annotation = new PlaceName(jcas);

						annotation.setBegin(localBegin - localPos -1);
						annotation.setEnd(localBegin - localPos);
						annotation.addToIndexes();
					}
	
					localPos = matcher.end();
				}
			}
		}

		// 溱、播、溪、思、費等州
		// 曆襄、鄧、宋、曹等州
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		logger.log(Level.FINE, "Total place names: " + totalPlaceNames);
	}
}
