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

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.cas.Type;
import org.apache.uima.UimaContext;

import eu.skqs.type.Text;


/*
 * Remove duplicate annotations from CAS. Only linear now.
 */
public class DeduplicatorAnalysisEngine extends JCasAnnotator_ImplBase {

	private Logger logger;

	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		logger = context.getLogger();
		logger.log(Level.INFO, "DeduplicatorAnalysisEngine initialize...");
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		logger.log(Level.INFO, "DeduplicatorAnalysisEngine process...");

		List<Annotation> annotationToRemoveList = new ArrayList<Annotation>();

		AnnotationIndex<Annotation> anAnnotationIndex = jcas.getAnnotationIndex();
		FSIterator<Annotation> anAnnotationIterator = anAnnotationIndex.iterator();

		Annotation prevAnnotation;
		Annotation nextAnnotation;

		if (anAnnotationIterator.hasNext()) {
			prevAnnotation = anAnnotationIterator.next();
			while (anAnnotationIterator.hasNext()) {
				nextAnnotation = anAnnotationIterator.next();

				String prevType = prevAnnotation.getType().getName();
				String nextType = nextAnnotation.getType().getName();

				if ((prevAnnotation.getBegin() == nextAnnotation.getBegin()) &&
				    (prevAnnotation.getEnd() == nextAnnotation.getEnd()) &&
				    (prevType.equals(nextType))) {
					annotationToRemoveList.add(nextAnnotation);
				}

				prevAnnotation = nextAnnotation;
			}
		}

                for (Annotation remove : annotationToRemoveList) {
                        remove.removeFromIndexes();
                }
	}
}
