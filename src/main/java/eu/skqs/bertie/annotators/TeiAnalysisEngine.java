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
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

import eu.skqs.bertie.cas.TeiCasSerializer;
import eu.skqs.type.SourceDocumentInformation;


// TeiCasConsumer
public class TeiAnalysisEngine extends JCasAnnotator_ImplBase {

	private Logger logger;

	public void initialize() throws ResourceInitializationException {

		logger = getContext().getLogger();
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

		String fileURI = null;
		FSIndex sourceIndex = aJCas.getAnnotationIndex(SourceDocumentInformation.type);
		FSIterator sourceIterator = sourceIndex.iterator();
		while (sourceIterator.hasNext()) {
			SourceDocumentInformation source = (SourceDocumentInformation)
			    sourceIterator.next();

			fileURI = source.getUri();
			break;
		}

		// TODO: fix tests
		if (fileURI == null)
			return;

		// TODO
		String fileName = fileURI;

		String encodingType = "UTF-8";
		PrintWriter fileWriter = null;
		try {
			fileWriter = new PrintWriter(fileName, encodingType);
			fileWriter.write(result);
			fileWriter.close();
		} catch (FileNotFoundException e) {
			logger.log(Level.WARNING, "File not found: ");
			throw new AnalysisEngineProcessException();
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.WARNING, "Unsupported encoding");
			throw new AnalysisEngineProcessException();
		} finally {
		}
	}
}
