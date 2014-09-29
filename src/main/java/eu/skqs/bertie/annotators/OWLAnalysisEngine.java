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

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

import eu.skqs.type.SourceDocumentInformation;
import eu.skqs.type.PersName;
import eu.skqs.type.PlaceName;
import eu.skqs.type.Name;


public class OWLAnalysisEngine extends JCasAnnotator_ImplBase {

	private Logger logger;

	@Override
	public void initialize(UimaContext uimaContext) throws ResourceInitializationException {

		logger = uimaContext.getLogger();
		logger.log(Level.FINE, "OWLAnalysisEngine initialize...");
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		logger.log(Level.FINE, "OWLAnalysisEngine process...");

		FSIndex persNameIndex = jcas.getAnnotationIndex(PersName.type);
		FSIterator persNameIterator = persNameIndex.iterator();
		while (persNameIterator.hasNext()) {
			PersName annotation = (PersName)persNameIterator.next();

			String persName = annotation.getCoveredText();

			System.out.println("	ClassAssertion(:Person :" + persName + ")");
		}

		FSIndex nameIndex = jcas.getAnnotationIndex(Name.type);
		FSIterator nameIterator = nameIndex.iterator();
		while (nameIterator.hasNext()) {
			Name annotation = (Name)nameIterator.next();

			String name = annotation.getCoveredText();
			String key = annotation.getKey();
			String type = annotation.getTEItype();

			System.out.println("	ObjectPropertyAssertion(:" + type + " :" + key + " :" + name + ")");
		}

		FSIndex placeNameIndex = jcas.getAnnotationIndex(PlaceName.type);
		FSIterator placeNameIterator = placeNameIndex.iterator();
		while (placeNameIterator.hasNext()) {
			PlaceName annotation = (PlaceName)placeNameIterator.next();

			String placeName = annotation.getCoveredText();

			System.out.println("	ClassAssertion(:Place :" + placeName + ")");
		}

	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		logger.log(Level.FINE, "OWLAnalysisEngine collectionProcessComplete...");
	}
}
