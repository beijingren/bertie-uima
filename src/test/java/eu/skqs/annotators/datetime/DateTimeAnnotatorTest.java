package eu.skqs.annotators.datetime;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.test.junit_extension.JUnitExtension;
import org.apache.uima.util.FileUtils;

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

		AnalysisEngine datetimeAnnotatorAE = AnalysisEngineFactory
		    .createEngine(DateTimeAnalysisEngine.class);

		datetimeAnnotatorAE.process(aJCas);

		// assertEquals(1, JCasUtil.select(aJCas, Dynasty.class).size());
	}
}
