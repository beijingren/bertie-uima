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

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;


public class PreprocessAnalysisEngine extends JCasAnnotator_ImplBase {

	// Logger
	private Logger logger;

	// Types to remove
	public static final String PARAM_REMOVETYPES = "RemoveTypes";

	private String mRemoveTypes;

	@Override
	public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
		super.initialize(uimaContext);

		logger = uimaContext.getLogger();
		logger.log(Level.FINE, "PreprocessAnalysisEngine initialize...");

		mRemoveTypes = ((String)uimaContext.getConfigParameterValue(PARAM_REMOVETYPES)).trim();
		System.out.println(mRemoveTypes);
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		logger.log(Level.FINE, "PreprocessAnalysisEngine process...");

		if ("".equals(mRemoveTypes)) {
			return;
		}

		// Remove previous annotations if wanted
		TypeSystem typeSystem = jcas.getTypeSystem();
		if (typeSystem == null) {
			logger.log(Level.WARNING, "   Can't get type system");
			return;
		}

		List<Annotation> annotationsToRemoveList = new ArrayList<Annotation>();

		String[] typeArray = mRemoveTypes.split(",");
		for (String typeString : typeArray) {

			Type type = typeSystem.getType("eu.skqs.type." + typeString);
			if (type == null) {
				continue;
			}

			FSIterator<Annotation> removeIterator =
			    jcas.getAnnotationIndex(type).iterator();
			while (removeIterator.hasNext()) {
				Annotation annotation = removeIterator.next();

				annotationsToRemoveList.add(annotation);
			}
		}

		for (Annotation annotation : annotationsToRemoveList) {
			annotation.removeFromIndexes();
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
	}
}
