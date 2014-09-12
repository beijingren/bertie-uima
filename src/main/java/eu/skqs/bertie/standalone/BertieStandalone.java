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

package eu.skqs.bertie.standalone;

import static org.apache.uima.fit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XCASSerializer;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.factory.TypePrioritiesFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypePriorities;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.XMLSerializer;

import org.xml.sax.SAXException;

import eu.skqs.bertie.annotators.DateTimeAnalysisEngine;
import eu.skqs.bertie.annotators.InterpunctionAnalysisEngine;
import eu.skqs.bertie.annotators.NumberUnitAnalysisEngine;
import eu.skqs.bertie.annotators.PersNameAnalysisEngine;
import eu.skqs.bertie.annotators.PlaceNameAnalysisEngine;
import eu.skqs.bertie.cas.TeiCasSerializer;
import eu.skqs.type.Body;
import eu.skqs.type.Div;
import eu.skqs.type.P;
import eu.skqs.type.Tei;
import eu.skqs.type.Text;


public class BertieStandalone {

	private static Logger logger = Logger.getLogger("BertieStandalone");

	public BertieStandalone() {
	}

	public String process(String document) throws Exception {
		logger.log(Level.INFO, "Processing started.");

		int startPosition = 0;
		int endPosition = document.length();

		// Generate jcas object
		JCas jcas = null;
		TypePriorities typePriorities = null;
		try {
			typePriorities = TypePrioritiesFactory.createTypePriorities();
		} catch (ResourceInitializationException e) {
			e.printStackTrace();
		}

		try {
			CAS cas = CasCreationUtils.createCas(createTypeSystemDescription(),
			    typePriorities, null);
			jcas = cas.getJCas();
			jcas.setDocumentText(document);
			logger.log(Level.FINE, "CAS object generated.");
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.WARNING, "CAS object could not be generated.");
		}

		Tei tei = new Tei(jcas);
		tei.setBegin(startPosition);
		tei.setEnd(endPosition);
		tei.setTitle("Analysis Result");
		tei.setTitleEn("Analysis Result");
		tei.setAuthor("UIMA bertie 0.0.1");
		tei.addToIndexes();

		// Warp the text fragment into valid TEI
		Body body = new Body(jcas);
		body.setBegin(startPosition);
		body.setEnd(endPosition);
		body.addToIndexes();

		Text text = new Text(jcas);
		text.setBegin(startPosition);
		text.setEnd(endPosition);
		text.addToIndexes();

		Div div = new Div(jcas);
		div.setBegin(startPosition);
		div.setEnd(endPosition);
		div.addToIndexes();

		P p = new P(jcas);
		p.setBegin(startPosition);
		p.setEnd(endPosition);
		p.addToIndexes();

		// Run engines
		String result = null;

		AnalysisEngine engine1 = AnalysisEngineFactory
		    .createEngine(InterpunctionAnalysisEngine.class);

		AnalysisEngine engine2 = AnalysisEngineFactory
		    .createEngine(NumberUnitAnalysisEngine.class);

		AnalysisEngine engine3 = AnalysisEngineFactory
		    .createEngine(PersNameAnalysisEngine.class);

		AnalysisEngine engine4 = AnalysisEngineFactory
		    .createEngine(PlaceNameAnalysisEngine.class);

		// Depends on PersName
		AnalysisEngine engine5 = AnalysisEngineFactory
		    .createEngine(DateTimeAnalysisEngine.class);

		SimplePipeline.runPipeline(jcas, engine1, engine2, engine3,
		    engine4, engine5);

		XCASSerializer ser = new XCASSerializer(jcas.getTypeSystem());
		OutputStream outputStream = new ByteArrayOutputStream();
		XMLSerializer xmlSer = new XMLSerializer(outputStream);

		TeiCasSerializer teiSer = new TeiCasSerializer();

		result = teiSer.serialize(jcas, xmlSer.getContentHandler());
/*
		try {
		} catch (SAXException e) {
			e.printStackTrace();
		}
*/
//		System.out.println(outputStream.toString());

		return result;
	}

	public static void main(String[] args) {
		String documentPath = null;

		// First arg must be the document path for now
		if (args.length != 1) {
			logger.log(Level.WARNING, "No document path given. Quitting.");
			System.exit(-1);
		} else {
			documentPath = args[0];
		}

		// Make sure we have a document path
		if (documentPath == null) {
			logger.log(Level.WARNING, "Document path is empty. Quitting.");
			System.exit(-1);
		}

		// Read the document
		try {
			String encodingType = "UTF-8";

			logger.log(Level.INFO, "Reading document " + documentPath);

			BufferedReader fileReader = new BufferedReader(
			    new InputStreamReader(new FileInputStream(documentPath), encodingType));

			StringBuilder sb = new StringBuilder();
			String line = null;

			while ((line = fileReader.readLine()) != null) {
				sb.append(line);
			}

			String input = sb.toString();
			fileReader.close();

			BertieStandalone standalone = new BertieStandalone();

			String output = standalone.process(input);
			if (output == null) {
				logger.log(Level.WARNING, "Empty processing result.");
				System.exit(-1);
			}

			PrintWriter fileWriter = new PrintWriter(documentPath, encodingType);
			fileWriter.write(output);
			fileWriter.close();

			// logger.log(Level.INFO, output);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
