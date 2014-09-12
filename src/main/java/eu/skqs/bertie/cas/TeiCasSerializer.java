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

import java.io.StringWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import eu.skqs.type.Tei;


public class TeiCasSerializer {

	// TODO
	public TeiCasSerializer() {
	}

	public TeiCasSerializer(TypeSystem aTypeSystem) {
	}

	public static String serialize(JCas aJCas, ContentHandler contentHandler) {

		String[] prioritizedTypeNames = new String[] { "eu.skqs.type.Body",
		    "eu.skqs.type.Text", "eu.skqs.type.Div", "eu.skqs.type.P" };

		String documentText = aJCas.getDocumentText();

		int startPosition = 0;
		int endPosition = documentText.length();

		FSIterator annotationIterator = aJCas.getAnnotationIndex().iterator();

		DocumentBuilderFactory documentBuilderFactory = null;
		DocumentBuilder documentBuilder = null;
		Document document = null;

		try {
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			document = documentBuilder.newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		Element lastElement = null;
		Element rootElement = null;
		while (annotationIterator.hasNext()) {
			Annotation annotation = (Annotation)annotationIterator.next();
			String annotationName = annotation.getType().getName();

			String tagName = "TEST";

			if (annotationName.equals("uima.tcas.DocumentAnnotation")) {
				continue;
			}

			if (annotationName.equals("eu.skqs.type.Interpunction")) {
				tagName = "pc";
			} else if (annotationName.equals("eu.skqs.type.Num")) {
				tagName = "num";
			} else if (annotationName.equals("eu.skqs.type.PersName")) {
				tagName = "persName";
			} else if (annotationName.equals("eu.skqs.type.PlaceName")) {
				tagName = "placeName";
			} else if (annotationName.equals("eu.skqs.type.Dynasty")) {
				tagName = "time";
			} else if (annotationName.equals("eu.skqs.type.P")) {
				tagName = "p";
			} else if (annotationName.equals("eu.skqs.type.Div")) {
				tagName = "div";
			} else if (annotationName.equals("eu.skqs.type.Body")) {
				tagName = "body";
			} else if (annotationName.equals("eu.skqs.type.Text")) {
				tagName = "text";
			} else if (annotationName.equals("eu.skqs.type.Tei")) {
				Tei tei = (Tei)annotation;

				rootElement = document.createElement("TEI");
				rootElement.setAttribute("xmlns", "http://www.tei-c.org/ns/1.0");
				document.appendChild(rootElement);

				Element teiHeader = document.createElement("teiHeader");
				Element fileDesc = document.createElement("fileDesc");
				Element titleStmt = document.createElement("titleStmt");
				Element titleZh = document.createElement("title");
				Element titleEn = document.createElement("title");
				Element author = document.createElement("author");
				Element name = document.createElement("name");
				Element choice = document.createElement("choice");
				Element sic = document.createElement("sic");
				Element publicationStmt = document.createElement("publicationStmt");
				Element publicationStmtP = document.createElement("p");
				Element sourceDesc = document.createElement("sourceDesc");
				Element sourceDescP = document.createElement("p");

				Text titleZhContent = document.createTextNode(tei.getTitle());
				titleZh.setAttribute("xml:lang", "zh");
				titleZh.appendChild(titleZhContent);

				Text titleEnContent = document.createTextNode(tei.getTitleEn());
				titleEn.setAttribute("xml:lang", "en");
				titleEn.appendChild(titleEnContent);

				Text sicContent = document.createTextNode(tei.getAuthor());
				sic.appendChild(sicContent);

				choice.appendChild(sic);
				name.appendChild(choice);
				author.appendChild(name);

				titleStmt.appendChild(titleZh);
				titleStmt.appendChild(titleEn);
				titleStmt.appendChild(author);

				Text publicationStmtPContent = document.createTextNode("XXX");
				publicationStmtP.appendChild(publicationStmtPContent);

				publicationStmt.appendChild(publicationStmtP);

				Text sourceDescPContent = document.createTextNode("XXX");
				sourceDescP.appendChild(sourceDescPContent);

				sourceDesc.appendChild(sourceDescP);

				fileDesc.appendChild(titleStmt);
				fileDesc.appendChild(publicationStmt);
				fileDesc.appendChild(sourceDesc);

				teiHeader.appendChild(fileDesc);

				rootElement.appendChild(teiHeader);

				lastElement = rootElement;
				continue;
			}

			Element element = document.createElement(tagName);

			if (annotationName.equals("eu.skqs.type.P") |
			    annotationName.equals("eu.skqs.type.Div") |
			    annotationName.equals("eu.skqs.type.Body") |
			    annotationName.equals("eu.skqs.type.Text")) {
				lastElement.appendChild(element);
				lastElement = element;
			} else {
				Text content = document.createTextNode(annotation.getCoveredText());
				element.appendChild(content);
				lastElement.appendChild(element);
			}

			endPosition = annotation.getBegin();

			if (endPosition > startPosition) {
				Text documentContent = document.createTextNode(
				    "\n" + documentText.substring(startPosition, endPosition) + "\n");
				lastElement.appendChild(documentContent);
			}


			startPosition = annotation.getEnd();
		}

		// Copy the rest
		Text documentContent = document.createTextNode(
		    documentText.substring(startPosition, documentText.length()));
		lastElement.appendChild(documentContent);

		// Transform DOM into XML
		StringWriter result = new StringWriter();
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(document);
			StreamResult xmlresult = new StreamResult(result);
			transformer.transform(source, xmlresult);
		} catch (TransformerException e) {
			e.printStackTrace();
		} finally {
			try {
				result.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return result.toString();
	}
}
