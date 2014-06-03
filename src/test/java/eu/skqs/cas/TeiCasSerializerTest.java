package eu.skqs.annotators.tei;

import java.io.File;
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

import eu.skqs.type.Dynasty;
import eu.skqs.annotators.datetime.DateTimeAnalysisEngine;


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

		AnalysisEngine datetimeAE = AnalysisEngineFactory
		    .createEngine(DateTimeAnalysisEngine.class);

		AnalysisEngine teiAE = AnalysisEngineFactory
		    .createEngine(TeiAnalysisEngine.class);

		SimplePipeline.runPipeline(aJCas, datetimeAE, teiAE);

		assertEquals(149, JCasUtil.select(aJCas, Dynasty.class).size());
	}
}
