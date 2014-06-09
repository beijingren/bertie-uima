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

package eu.skqs.bertie.collection;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.test.junit_extension.JUnitExtension;
import org.apache.uima.fit.component.CasDumpWriter;

import org.junit.Test;

import eu.skqs.bertie.collection.TeiCollectionReader;
import eu.skqs.bertie.annotators.DateTimeAnalysisEngine;


public class TeiCollectionReaderTest {

	@Test
	public void testTeiCollectionReader() throws Exception {

		CollectionReaderDescription reader =
		    CollectionReaderFactory.createReaderDescription(
		    TeiCollectionReader.class,
		    TeiCollectionReader.PARAM_INPUTDIR,
		    JUnitExtension.getURL("texts/TeiCollectionReader").getPath());

		AnalysisEngineDescription dump =
		    AnalysisEngineFactory.createEngineDescription(
		    CasDumpWriter.class,
		    CasDumpWriter.PARAM_OUTPUT_FILE, "/tmp/uima-dump.txt");

		SimplePipeline.runPipeline(reader, dump);
	}
}
