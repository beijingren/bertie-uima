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

import java.util.Map;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

import eu.skqs.bertie.resources.RhymeSharedResource;
import eu.skqs.type.SourceDocumentInformation;
import eu.skqs.type.Lg;
import eu.skqs.type.L;
import eu.skqs.type.Rhyme;


public class RimeAnalysisEngine extends JCasAnnotator_ImplBase {

	// Logger
	private Logger logger;

	// Shared resources
	public final static String MODEL_KEY = "RimeResource";
	@ExternalResource(key = MODEL_KEY)
	private RhymeSharedResource sparqlSharedResource;

	// Rimes
	private Map<String, List<String>> mRimesMap;

	@Override
	public void initialize(UimaContext uimaContext) throws ResourceInitializationException {

		logger = uimaContext.getLogger();
		logger.log(Level.FINE, "RimeAnalysisEngine initialize...");

		// Load shared resource
		mRimesMap = sparqlSharedResource.getRimes();
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		logger.log(Level.FINE, "RimeAnalysisEngine process...");

		// TODO: lg type="poem" ?
		FSIndex lineIndex = jcas.getAnnotationIndex(L.type);
		FSIterator lineIterator = lineIndex.iterator();
		while (lineIterator.hasNext()) {
			L annotation = (L)lineIterator.next();

			String line = annotation.getCoveredText();
			Integer lineLength = line.length();

			if (lineLength > 0) {

				// End rhyme
				String rhyme = line.substring(lineLength-1, lineLength);
				List<String> rimeList = mRimesMap.get(rhyme);

				if (rimeList != null) {
					Rhyme rhymeAnnotation = new Rhyme(jcas);

					rhymeAnnotation.setLabel(rimeList.get(0));
					rhymeAnnotation.setTone(rimeList.get(1));
					rhymeAnnotation.setBegin(annotation.getBegin()+lineLength-1);
					rhymeAnnotation.setEnd(annotation.getBegin()+lineLength);
					rhymeAnnotation.addToIndexes();
				}

				for (int i = 0; i < lineLength-1; i++) {
					String lineChar = line.substring(i, i+1);

					rimeList = mRimesMap.get(lineChar);
					if (rimeList != null) {
						Rhyme rhymeAnnotation = new Rhyme(jcas);

						// rhymeAnnotation.setLabel(rimeList.get(0));
						rhymeAnnotation.setTone(rimeList.get(1));
						rhymeAnnotation.setBegin(annotation.getBegin()+i);
						rhymeAnnotation.setEnd(annotation.getBegin()+i+1);
						rhymeAnnotation.addToIndexes();
					}
				}
			}
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		logger.log(Level.FINE, "RimeAnalysisEngine collectionProcessComplete...");
	}
}
