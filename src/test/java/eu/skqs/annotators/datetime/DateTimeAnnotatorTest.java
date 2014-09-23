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
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;


import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.test.junit_extension.JUnitExtension;
import org.apache.uima.util.FileUtils;
import static org.apache.uima.fit.factory.ExternalResourceFactory.bindResource;

import eu.skqs.bertie.resources.SPARQLSharedResource;
import eu.skqs.type.Dynasty;


public class DateTimeAnnotatorTest {

	// Text to be analysed
	private String documentText = null;

	@Before
	public void setUp() throws IOException {

		File textFile = JUnitExtension.getFile("texts/四庫全書總目提要/015.txt");
		documentText = FileUtils.file2String(textFile, "utf-8");
	}

	@Test
	public void testDynasties() throws Exception {
		JCas aJCas = JCasFactory.createJCas();

		aJCas.setDocumentText(documentText);

		AnalysisEngineDescription engine0 =
		    AnalysisEngineFactory.createEngineDescription(
		    DateTimeAnalysisEngine.class);

		String owlPath = "/docker/dublin-store/rdf/sikuquanshu.rdf";

		// Shared resource
		bindResource(engine0, DateTimeAnalysisEngine.MODEL_KEY,
		    SPARQLSharedResource.class, owlPath);

		SimplePipeline.runPipeline(aJCas, engine0);

		// assertEquals(1, JCasUtil.select(aJCas, Dynasty.class).size());
	}
}
