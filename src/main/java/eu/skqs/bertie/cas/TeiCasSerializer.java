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
import java.io.StringReader;
import java.io.IOException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.uima.cas.CASException;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasToInlineXml;

import org.xml.sax.ContentHandler;


public class TeiCasSerializer {
	private static final String TYPE_MEASURE = "eu.skqs.type.Measure";

	// TODO
	public TeiCasSerializer() {
	}

	public TeiCasSerializer(TypeSystem aTypeSystem) {
	}

	public static String serialize(JCas aJCas, ContentHandler contentHandler) {

		CasToInlineXml cas2xml = new CasToInlineXml();
		String document = null;
		try {
			document = cas2xml.generateXML(aJCas.getCas());
		} catch (CASException e) {
			e.printStackTrace();
		}

		// TODO: read from inside
		String xslPath = "/docker/bertie-uima/src/main/resources/xsl/cas2tei.xsl";

		// Transform CAS XML into TEI
		StringReader casXml = new StringReader(document);
		StringWriter result = new StringWriter();
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer =
			    transformerFactory.newTransformer(new StreamSource(xslPath));

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			StreamResult xmlresult = new StreamResult(result);

			transformer.transform(new StreamSource(casXml), xmlresult);
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
