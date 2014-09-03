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

package eu.skqs.bertie.cas;

import java.io.OutputStream;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.cas.Type;

import org.apache.uima.internal.util.XmlElementName;

import org.xml.sax.ContentHandler;

import eu.skqs.type.Dynasty;
import eu.skqs.type.Interpunction;


public class TeiCasSerializer {

	// TODO
	public TeiCasSerializer() {
	}

	public TeiCasSerializer(TypeSystem aTypeSystem) {
	}

	public static String serialize(JCas aJCas, ContentHandler contentHandler) {

		String documentText = aJCas.getDocumentText();
		String result = "";
		int startPosition = 0;
		int endPosition = documentText.length();

		FSIterator annotationIterator = aJCas.getAnnotationIndex().iterator();

		while (annotationIterator.hasNext()) {
			Annotation annotation = (Annotation)annotationIterator.next();

			String annotationName = annotation.getType().getName();
			String tagName = null;

			if (annotationName.equals("eu.skqs.type.Interpunction")) {
				tagName = "pc";
				endPosition = annotation.getBegin();

				result += documentText.substring(startPosition, endPosition);
				result += "\n";
				result += "<" + tagName + ">";
				result += annotation.getCoveredText();
				result += "</" + tagName + ">";
				result += "\n";

				startPosition = annotation.getEnd();
			} else if (annotationName.equals("eu.skqs.type.Num")) {
				tagName = "num";
				endPosition = annotation.getBegin();

				result += documentText.substring(startPosition, endPosition);
				result += "\n";
				result += "<" + tagName + ">";
				result += annotation.getCoveredText();
				result += "</" + tagName + ">";
				result += "\n";

				startPosition = annotation.getEnd();
			} else if (annotationName.equals("eu.skqs.type.PersName")) {
				tagName = "persName";
				endPosition = annotation.getBegin();

				result += documentText.substring(startPosition, endPosition);
				result += "\n";
				result += "<" + tagName + ">";
				result += annotation.getCoveredText();
				result += "</" + tagName + ">";
				result += "\n";

				startPosition = annotation.getEnd();
			} else if (annotationName.equals("eu.skqs.type.PlaceName")) {
				tagName = "placeName";
				endPosition = annotation.getBegin();

				result += documentText.substring(startPosition, endPosition);
				result += "\n";
				result += "<" + tagName + ">";
				result += annotation.getCoveredText();
				result += "</" + tagName + ">";
				result += "\n";

				startPosition = annotation.getEnd();
			}

		}

		return result;
	}
}
