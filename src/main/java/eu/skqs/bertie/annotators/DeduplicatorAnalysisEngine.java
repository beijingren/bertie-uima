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

import eu.skqs.type.Text;

/*
 * This code is from UIMA common by Nicolas Hernandez.
 */
public class DeduplicatorAnalysisEngine extends JCasAnnotator_ImplBase {

	private Logger logger;

	private List<Annotation> annotationToRemoveList = new ArrayList<Annotation>();

	public void initialize() throws ResourceInitializationException {

		logger = getContext().getLogger();
	}

	public void removeDuplicateType(JCas aJCas, int subsumingType, int subsumedType) {

AnnotationIndex<Annotation> subsumingAnnotationIndex = aJCas.getAnnotationIndex(subsumingType);

AnnotationIndex<Annotation> subsumedAnnotationIndex = aJCas.getAnnotationIndex(subsumedType);

                FSIterator<Annotation> subsumingAnnotationIterator = subsumingAnnotationIndex.iterator();

                while (subsumingAnnotationIterator.hasNext()) {
                        Annotation aSubsumingAnnotation = subsumingAnnotationIterator.next();
                        FSIterator<Annotation> subsumedAnnotationIterator = subsumedAnnotationIndex.subiterator(aSubsumingAnnotation);
                        while (subsumedAnnotationIterator.hasNext()) {
                                Annotation aSubsumedAnnotation = subsumedAnnotationIterator.next();
                               if ((aSubsumingAnnotation.getBegin() == aSubsumedAnnotation.getBegin()) && (aSubsumingAnnotation.getEnd() == aSubsumedAnnotation.getEnd()) ) {
                                                annotationToRemoveList.add(aSubsumedAnnotation);
                             }
                        }
                }


	}
	public void removeDuplicate(JCas aJCas, String subsumingAnnotation, String subsumedAnnotation) {
			List<Annotation> annotationToRemoveList = new ArrayList<Annotation>();

                Type subsumingAnnotationType = aJCas.getTypeSystem().getType(subsumingAnnotation);
                AnnotationIndex<Annotation> subsumingAnnotationIndex = aJCas.getAnnotationIndex(subsumingAnnotationType);

                Type subsumedAnnotationType = aJCas.getTypeSystem().getType(subsumedAnnotation);
                AnnotationIndex<Annotation> subsumedAnnotationIndex = aJCas.getAnnotationIndex(subsumedAnnotationType);

                FSIterator<Annotation> subsumingAnnotationIterator = subsumingAnnotationIndex.iterator();

                while (subsumingAnnotationIterator.hasNext()) {
                        Annotation aSubsumingAnnotation = subsumingAnnotationIterator.next();
                        FSIterator<Annotation> subsumedAnnotationIterator = subsumedAnnotationIndex.subiterator(aSubsumingAnnotation);
                        while (subsumedAnnotationIterator.hasNext()) {
                                Annotation aSubsumedAnnotation = subsumedAnnotationIterator.next();
                               if ((aSubsumingAnnotation.getBegin() == aSubsumedAnnotation.getBegin()) && (aSubsumingAnnotation.getEnd() == aSubsumedAnnotation.getEnd()) ) {
                                                annotationToRemoveList.add(aSubsumedAnnotation);
                             }
                        }
                }


                for (Annotation remove : annotationToRemoveList) {
                        remove.removeFromIndexes();
                }


	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		AnnotationIndex<Annotation> anAnnotationIndex = jcas.getAnnotationIndex();
		FSIterator<Annotation> anAnnotationIterator = anAnnotationIndex.iterator();

		Annotation prevAnnotation;
		Annotation nextAnnotation;

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
			System.out.println("--- Type: " + prevAnnotation.getType());
			System.out.println("--- Type text: " + Text.type);
                }

                for (Annotation remove : annotationToRemoveList) {
                        remove.removeFromIndexes();
                }
	}
}
