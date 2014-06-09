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

package eu.skqs.annotators.tei;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.XMLSerializer;

import eu.skqs.cas.TeiCasSerializer;


// TeiCasConsumer
public class TeiAnalysisEngine extends JCasAnnotator_ImplBase {

	public void initialize() throws ResourceInitializationException {

	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		try {
			writeTei(aJCas);
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}


	private void writeTei(JCas aJCas) throws IOException {

		FileOutputStream aStream = null;

		try {
			aStream = new FileOutputStream("XXX.xml");
			TeiCasSerializer teiSerializer = new TeiCasSerializer(aJCas.getTypeSystem());
			XMLSerializer xmlSerializer = new XMLSerializer(aStream, false);

			teiSerializer.serialize(aJCas, xmlSerializer.getContentHandler());
		} finally {
			if (aStream != null) {
				aStream.close();
			}
		}
	}
}