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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.test.junit_extension.JUnitExtension;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.XMLSerializer;
import org.apache.uima.cas.impl.XCASSerializer;

import org.xml.sax.SAXException;

import eu.skqs.type.Dynasty;
import eu.skqs.type.Tei;
// import eu.skqs.annotators.datetime.DateTimeAnalysisEngine;
import eu.skqs.bertie.annotators.InterpunctionAnalysisEngine;
import eu.skqs.bertie.annotators.TeiAnalysisEngine;


public class TeiCasSerializerTest {

	// Text to be analysed
	private String documentText = null;

	@Before
	public void setUp() throws IOException {
		File textFile = JUnitExtension.getFile("texts/四庫全書總目提要/015.txt");
		documentText = FileUtils.file2String(textFile, "utf-8");
	}

	@Test
	public void testTei() throws Exception {
		JCas aJCas = JCasFactory.createJCas();

		aJCas.setDocumentText(documentText);

		Tei tei = new Tei(aJCas);
		tei.setBegin(0);
		tei.setEnd(documentText.length());
		tei.setTitle("XXX");
		tei.setTitleEn("XXX");
		tei.setAuthor("XXX");
		tei.addToIndexes();

//		AnalysisEngine datetimeAE = AnalysisEngineFactory
//		    .createEngine(DateTimeAnalysisEngine.class);

		AnalysisEngine interpunctionAE = AnalysisEngineFactory
		    .createEngine(InterpunctionAnalysisEngine.class);

		AnalysisEngine teiAE = AnalysisEngineFactory
		    .createEngine(TeiAnalysisEngine.class);

		SimplePipeline.runPipeline(aJCas, interpunctionAE, teiAE);

		XCASSerializer ser = new XCASSerializer(aJCas.getTypeSystem());
		OutputStream outputStream = new FileOutputStream("/tmp/xcas");
		XMLSerializer xmlSer = new XMLSerializer(outputStream);

		try {
			ser.serialize(aJCas.getCas(), xmlSer.getContentHandler());
		} catch (SAXException e) {
			e.printStackTrace();
		}

		// assertEquals(149, JCasUtil.select(aJCas, Dynasty.class).size());
	}
}
