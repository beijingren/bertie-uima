/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */

package eu.skqs.bertie.util;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.uima.cas.ConstraintFactory;
import org.apache.uima.cas.FSIntConstraint;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FSMatchConstraint;
import org.apache.uima.cas.FSTypeConstraint;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeaturePath;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.cas.TOP;
//import org.apache.uima.jcas.tcas.DocumentAnnotation;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * <p>
 * 
 * @author Philip Ogren
 * @author Philipp Wetzler
 * @author Steven Bethard
 */
public class AnnotationRetrieval {

	public static Type getCasType(JCas jCas, Class<? extends TOP> cls) {
		try {
			return jCas.getCasType(cls.getField("type").getInt(null));
		}
		catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
		catch (NoSuchFieldException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Finds and returns the annotation of the provided type that is adjacent to
	 * the focus annotation in either to the left or right. Adjacent simply
	 * means the last annotation of the passed in type that ends before the
	 * start of the focus annotation (for the left side) or the first annotation
	 * that starts after the end of the focus annotation (for the right side).
	 * Thus, adacent refers to order of annotations at the annotation level
	 * (e.g. give me the first annotation of type x to the left of the focus
	 * annotation) and does not mean that the annotations are adjacenct at the
	 * character offset level.
	 * 
	 * <br>
	 * <b>note:</b> This method runs <b>much</b> faster if the type of the
	 * passed in annotation and the passed in type are the same.
	 * 
	 * @param jCas
	 * @param focusAnnotation
	 *            an annotation that you want to find an annotation adjacent to
	 *            this.
	 * @param adjacentClass
	 *            the type of annotation that you want to find
	 * @param adjacentBefore
	 *            if true then returns an annotation to the left of the passed
	 *            in annotation, otherwise an annotation to the right will be
	 *            returned.
	 * @return an annotation of type adjacentType or null
	 */
	public static <T extends Annotation> T getAdjacentAnnotation(JCas jCas, Annotation focusAnnotation,
			Class<T> adjacentClass, boolean adjacentBefore) {
		try {
			Type adjacentType = getCasType(jCas, adjacentClass);
			if (focusAnnotation.getType().equals(adjacentType)) {
				FSIterator<Annotation> iterator = jCas.getAnnotationIndex(adjacentType).iterator();
				iterator.moveTo(focusAnnotation);
				if (adjacentBefore) iterator.moveToPrevious();
				else iterator.moveToNext();
				return adjacentClass.cast(iterator.get());
			}
			else {
				FSIterator<Annotation> cursor = jCas.getAnnotationIndex().iterator();
				cursor.moveTo(focusAnnotation);
				if (adjacentBefore) {
					while (cursor.isValid()) {
						cursor.moveToPrevious();
						Annotation annotation = (Annotation) cursor.get();
						if (adjacentClass.isInstance(annotation) && annotation.getEnd() == focusAnnotation.getBegin()) return adjacentClass
						.cast(annotation);
					}
				}
				else {
					while (cursor.isValid()) {
						cursor.moveToNext();
						Annotation annotation = (Annotation) cursor.get();
						if (adjacentClass.isInstance(annotation) && annotation.getBegin() == focusAnnotation.getEnd()) return adjacentClass
						.cast(annotation);
					}
				}
			}
		}
		catch (NoSuchElementException nsee) {
			return null;
		}
		return null;
	}
}
