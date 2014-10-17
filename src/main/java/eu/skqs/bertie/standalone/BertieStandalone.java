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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XCASSerializer;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.factory.TypePrioritiesFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypePriorities;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.XMLSerializer;
import static org.apache.uima.fit.factory.ExternalResourceFactory.bindResource;

import org.xml.sax.SAXException;

import eu.skqs.cas.TeiDeserializer;
import eu.skqs.type.SourceDocumentInformation;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

// TODO: rename consumers.TeiConsumer or similar
import eu.skqs.bertie.annotators.TeiAnalysisEngine;

import eu.skqs.bertie.annotators.AuxiliaryAnalysisEngine;
import eu.skqs.bertie.annotators.DateTimeAnalysisEngine;
import eu.skqs.bertie.annotators.DeduplicatorAnalysisEngine;
import eu.skqs.bertie.annotators.InterpunctionAnalysisEngine;
import eu.skqs.bertie.annotators.NumberUnitAnalysisEngine;
import eu.skqs.bertie.annotators.OWLAnalysisEngine;
import eu.skqs.bertie.annotators.PersNameAnalysisEngine;
import eu.skqs.bertie.annotators.PlaceNameAnalysisEngine;
import eu.skqs.bertie.annotators.PreprocessAnalysisEngine;
import eu.skqs.bertie.annotators.PreprocessPlainAnalysisEngine;
import eu.skqs.bertie.annotators.RimeAnalysisEngine;
import eu.skqs.bertie.cas.TeiCasSerializer;
import eu.skqs.bertie.collection.TeiCollectionReader;
import eu.skqs.bertie.resources.PersNameResource;
import eu.skqs.bertie.resources.PlaceNameResource;
import eu.skqs.bertie.resources.SPARQLSharedResource;

import eu.skqs.type.Body;
import eu.skqs.type.Chapter;
import eu.skqs.type.Div;
import eu.skqs.type.P;
import eu.skqs.type.Tei;
import eu.skqs.type.Text;


public class BertieStandalone {

	private static Logger logger = Logger.getLogger("BertieStandalone");
	private static String owlPath = "/docker/dublin-store/rdf/sikuquanshu.owl";
	private static String typesToRemove = "";
	private static String filePath;
	private static String analysisMode;

	private static boolean extractMode = false;
	private static boolean poetryMode = false;

	// TODO: move to initialize
	private static String newLine = System.getProperty("line.separator");

	public BertieStandalone() {
	}

	public void extractWithCollectionReader(String directory) throws Exception {
		logger.log(Level.INFO, "Extraction started.");

		// TEI reader
		CollectionReaderDescription reader =
		    CollectionReaderFactory.createReaderDescription(
		    TeiCollectionReader.class,
		    TeiCollectionReader.PARAM_INPUTDIR,
		    directory);

		AnalysisEngineDescription owl =
 		    AnalysisEngineFactory.createEngineDescription(
		    OWLAnalysisEngine.class);

		SimplePipeline.runPipeline(reader, owl);

	}

	public void processWithCollectionReader(String directory) throws Exception {
		logger.log(Level.INFO, "Processing with TEI collection reader started.");

		// TEI reader
		CollectionReaderDescription reader =
		    CollectionReaderFactory.createReaderDescription(
		    TeiCollectionReader.class,
		    TeiCollectionReader.PARAM_INPUTDIR,
		    directory);

		AnalysisEngineDescription engineA =
		    AnalysisEngineFactory.createEngineDescription(
		    PreprocessAnalysisEngine.class,
		    PreprocessAnalysisEngine.PARAM_REMOVETYPES,
		    typesToRemove);

		AnalysisEngineDescription engine0 =
 		    AnalysisEngineFactory.createEngineDescription(
		    AuxiliaryAnalysisEngine.class);

		// Shared resource
		bindResource(engine0, AuxiliaryAnalysisEngine.MODEL_KEY,
		    SPARQLSharedResource.class, owlPath);

		AnalysisEngineDescription engine1 =
		    AnalysisEngineFactory.createEngineDescription(
		    InterpunctionAnalysisEngine.class);

		AnalysisEngineDescription engine2 =
		    AnalysisEngineFactory.createEngineDescription(
		    NumberUnitAnalysisEngine.class);

		AnalysisEngineDescription engine3 =
		    AnalysisEngineFactory.createEngineDescription(
		    PersNameAnalysisEngine.class);

		// Shared resource
		bindResource(engine3, PersNameAnalysisEngine.MODEL_KEY,
		    PersNameResource.class, owlPath);

		AnalysisEngineDescription engine4 =
		    AnalysisEngineFactory.createEngineDescription(
		    DateTimeAnalysisEngine.class);

		// Shared resource
		bindResource(engine4, DateTimeAnalysisEngine.MODEL_KEY,
		    SPARQLSharedResource.class, owlPath);

		AnalysisEngineDescription engine5 =
		    AnalysisEngineFactory.createEngineDescription(
		    PlaceNameAnalysisEngine.class);

		// Shared resource
		bindResource(engine5, PlaceNameAnalysisEngine.MODEL_KEY,
		    PlaceNameResource.class, owlPath);

		// Rime
		AnalysisEngineDescription engine6 =
		    AnalysisEngineFactory.createEngineDescription(
		    RimeAnalysisEngine.class);

		// Shared resource
		bindResource(engine6, RimeAnalysisEngine.MODEL_KEY,
		    SPARQLSharedResource.class, owlPath);

		// DEBUG
		AnalysisEngineDescription dump =
		    AnalysisEngineFactory.createEngineDescription(
		    CasDumpWriter.class,
		    CasDumpWriter.PARAM_OUTPUT_FILE, "/tmp/uima-dump.txt");

		// Deduplication
		AnalysisEngineDescription deduplicator =
		    AnalysisEngineFactory.createEngineDescription(
		    DeduplicatorAnalysisEngine.class);

		// TEI serializer
		AnalysisEngineDescription writer =
		    AnalysisEngineFactory.createEngineDescription(
		    TeiAnalysisEngine.class);

		SimplePipeline.runPipeline(reader, engineA, engine0, engine1, engine2,
		    engine3, engine4, engine5, engine6, deduplicator, writer);
	}

	public static void processWithFile() throws Exception {
		logger.log(Level.INFO, "Processing started.");

		JCas jcas = null;
		CAS cas = null;
		TypePriorities typePriorities = null;

		// Generate jcas object
		try {
			typePriorities = TypePrioritiesFactory.createTypePriorities();
		} catch (ResourceInitializationException e) {
			e.printStackTrace();
		}

		try {
			cas = CasCreationUtils.createCas(createTypeSystemDescription(),
			    typePriorities, null);
			jcas = cas.getJCas();
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.WARNING, "CAS object could not be generated.");
		}

		// Read the document
		File currentFile = new File(filePath);
		FileInputStream inputStream = new FileInputStream(currentFile);

		SourceDocumentInformation annotation = new SourceDocumentInformation(jcas);
		annotation.setUri(currentFile.getAbsoluteFile().toString());
		annotation.addToIndexes();

		logger.log(Level.INFO, currentFile.getAbsoluteFile().toString());

		try {
			TeiDeserializer.deserialize(inputStream, cas);
		} catch (SAXException e) {
			e.printStackTrace();
			throw new Exception(e);
		} finally {
			inputStream.close();
		}

		AnalysisEngineDescription engine0 =
		    AnalysisEngineFactory.createEngineDescription(
		    AuxiliaryAnalysisEngine.class);

		// Shared resource
		bindResource(engine0, AuxiliaryAnalysisEngine.MODEL_KEY,
		    SPARQLSharedResource.class, owlPath);

		AnalysisEngineDescription engine1 =
		    AnalysisEngineFactory.createEngineDescription(
		    InterpunctionAnalysisEngine.class);

		AnalysisEngineDescription engine2 =
		    AnalysisEngineFactory.createEngineDescription(
		    NumberUnitAnalysisEngine.class);

		AnalysisEngineDescription engine3 =
		    AnalysisEngineFactory.createEngineDescription(
		    PersNameAnalysisEngine.class);

		// Shared resource
		bindResource(engine3, PersNameAnalysisEngine.MODEL_KEY,
		    PersNameResource.class, owlPath);

		AnalysisEngineDescription engine4 =
		    AnalysisEngineFactory.createEngineDescription(
		    DateTimeAnalysisEngine.class);

		// Shared resource
		bindResource(engine4, DateTimeAnalysisEngine.MODEL_KEY,
		    SPARQLSharedResource.class, owlPath);

		AnalysisEngineDescription engine5 =
		    AnalysisEngineFactory.createEngineDescription(
		    PlaceNameAnalysisEngine.class);

		// Shared resource
		bindResource(engine5, PlaceNameAnalysisEngine.MODEL_KEY,
		    PlaceNameResource.class, owlPath);

		SimplePipeline.runPipeline(jcas, engine0, engine1, engine2,
		    engine3, engine4, engine5);

		// Deduplication
		AnalysisEngineDescription deduplicator =
		    AnalysisEngineFactory.createEngineDescription(
		    DeduplicatorAnalysisEngine.class);

		// TEI serializer
		AnalysisEngineDescription writer =
		    AnalysisEngineFactory.createEngineDescription(
		    TeiAnalysisEngine.class);

		SimplePipeline.runPipeline(jcas, engine0, engine1, engine2, engine3, engine4, engine5, deduplicator, writer);
	}

	public String process(String document) throws Exception {
		logger.log(Level.INFO, "Processing started.");

		int startPosition = 0;
		int endPosition = document.length();

		JCas jcas = null;
		TypePriorities typePriorities = null;

		// Generate jcas object
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
		div.setN(1);
		div.setTeitype("chapter");
		div.addToIndexes();

		// Run engines
		String result = null;

		AnalysisEngineDescription preprocess =
		    AnalysisEngineFactory.createEngineDescription(
		    PreprocessPlainAnalysisEngine.class,
		    PreprocessPlainAnalysisEngine.PARAM_MODE,
		    analysisMode);

		AnalysisEngineDescription engine0 =
 		    AnalysisEngineFactory.createEngineDescription(
		    AuxiliaryAnalysisEngine.class);

		// Shared resource
		bindResource(engine0, AuxiliaryAnalysisEngine.MODEL_KEY,
		    SPARQLSharedResource.class, owlPath);

		AnalysisEngineDescription engine1 =
		    AnalysisEngineFactory.createEngineDescription(
		    InterpunctionAnalysisEngine.class);

		AnalysisEngineDescription engine2 =
		    AnalysisEngineFactory.createEngineDescription(
		    NumberUnitAnalysisEngine.class);

		AnalysisEngineDescription engine3 =
		    AnalysisEngineFactory.createEngineDescription(
		    PersNameAnalysisEngine.class);

		// Shared resource
		bindResource(engine3, PersNameAnalysisEngine.MODEL_KEY,
		    PersNameResource.class, owlPath);

		AnalysisEngineDescription engine4 =
		    AnalysisEngineFactory.createEngineDescription(
		    DateTimeAnalysisEngine.class);

		// Shared resource
		bindResource(engine4, DateTimeAnalysisEngine.MODEL_KEY,
		    SPARQLSharedResource.class, owlPath);

		AnalysisEngineDescription engine5 =
		    AnalysisEngineFactory.createEngineDescription(
		    PlaceNameAnalysisEngine.class);

		// Shared resource
		bindResource(engine5, PlaceNameAnalysisEngine.MODEL_KEY,
		    PlaceNameResource.class, owlPath);

		// Rhyme
		AnalysisEngineDescription engine6 =
		    AnalysisEngineFactory.createEngineDescription(
		    RimeAnalysisEngine.class);

		// Shared resource
		bindResource(engine6, RimeAnalysisEngine.MODEL_KEY,
		    SPARQLSharedResource.class, owlPath);

		SimplePipeline.runPipeline(jcas, preprocess, engine0, engine1, engine2,
		    engine3, engine4, engine5, engine6);
		TeiCasSerializer teiSer = new TeiCasSerializer();

		result = teiSer.serialize(jcas);

		return result;
	}

	public static void main(String[] args) {

		// Options
		Option file = OptionBuilder.withArgName("file")
				.withLongOpt("file")
				.hasArg()
				.withDescription("File to annotate")
				.create("f");

		Option directory = OptionBuilder.withArgName("directory")
				.withLongOpt("directory")
				.hasArg()
				.withDescription("Directory to annotate")
				.create("d");

		Option owl = OptionBuilder.withArgName("owl")
				.withLongOpt("owl")
				.hasArg()
				.withDescription("OWL file to use in annotation")
				.create("o");

		Option plain = OptionBuilder
				.withLongOpt("plain")
				.withDescription("Plain text file format")
				.create("p");

		Option tei = OptionBuilder
				.withLongOpt("tei")
				.withDescription("TEI file format")
				.create("t");

		Option mode = OptionBuilder.withArgName("extract|minimal|maximal|prosody")
				.withLongOpt("mode")
				.hasArg()
				.withDescription("Mode to operate in")
				.create("m");

		Option clean = OptionBuilder.withArgName("T0,T1,T3")
				.withLongOpt("clean")
				.hasArg()
				.withDescription("Remove gives types, MUST START UPPERCASE")
				.create("c");

		Options options = new Options();
		options.addOption(file);
		options.addOption(directory);
		options.addOption(owl);
		options.addOption(plain);
		options.addOption(tei);
		options.addOption(mode);
		options.addOption(clean);

		CommandLineParser parser = new GnuParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmdline = null;

		try {
			cmdline = parser.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		BertieStandalone standalone = new BertieStandalone();
		String documentPath = null;

		// Check for custom OWL
		if (cmdline.hasOption("owl")) {
			owlPath = cmdline.getOptionValue("owl");
		}

		// Check for clean
		if (cmdline.hasOption("clean")) {
			typesToRemove = cmdline.getOptionValue("clean");
		}

		// Check for mode
		if (cmdline.hasOption("mode")) {
			String currentMode= cmdline.getOptionValue("mode");

			if (currentMode.equals("extract")) {
				extractMode = true;
			} else if (currentMode.equals("poetry")) {
				poetryMode = true;
			}

			analysisMode = currentMode;
		}

		// Check for directory option
		if (cmdline.hasOption("directory")) {
			// We support TEI directorys only
			if (!cmdline.hasOption("tei")) {
				logger.log(Level.WARNING,
				    "TEI file format must be selected with directory argument");
				System.exit(-1);
			}


			String directoryPath = cmdline.getOptionValue("directory");

			if (extractMode) {
				try {
					standalone.extractWithCollectionReader(directoryPath);
				} catch (Exception e) {
				}

				System.exit(0);
			}

			try {
				standalone.processWithCollectionReader(directoryPath);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}

			System.exit(0);
		}

		// Check for file option
		if (cmdline.hasOption("file")) {
			// TODO: clean this up
			documentPath = cmdline.getOptionValue("file");
			filePath = cmdline.getOptionValue("file");

			// TEI
			if (cmdline.hasOption("tei")) {
				try {
					processWithFile();
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
				System.exit(0);
			}
	
			// Check for plain option
			if (!cmdline.hasOption("plain")) {
				logger.log(Level.WARNING,
				    "Plain text format must be selected with file argument");
				System.exit(-1);
			}
		} else {
			logger.log(Level.WARNING, "No file argument given. Quitting.");
			formatter.printHelp("bertie", options);
			System.exit(-1);
		}


		// Make sure we have a document path
		if (documentPath == null) {
			logger.log(Level.WARNING, "Document path is empty. Quitting.");
			System.exit(-1);
		}

		// Read the document
		try {
			String encodingType = "UTF-8";

			BufferedReader fileReader = new BufferedReader(
			    new InputStreamReader(new FileInputStream(documentPath), encodingType));

			StringBuilder sb = new StringBuilder();
			String line = null;

			while ((line = fileReader.readLine()) != null) {
				sb.append(line + newLine);
			}

			String input = sb.toString();
			fileReader.close();


			String output = standalone.process(input);
			if (output == null) {
				logger.log(Level.WARNING, "Empty processing result.");
				System.exit(-1);
			}

			PrintWriter fileWriter = new PrintWriter(documentPath, encodingType);
			fileWriter.write(output);
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
