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

import java.io.FileOutputStream;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import eu.skqs.bertie.cas.TeiCasSerializer;


// TeiCasConsumer
public class TeiAnalysisEngine extends JCasAnnotator_ImplBase {

	public void initialize() throws ResourceInitializationException {
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		String result = null;

		try {
			TeiCasSerializer teiSerializer = new TeiCasSerializer();
			result = teiSerializer.serialize(aJCas);
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}

		System.out.println(result);
	}
}
