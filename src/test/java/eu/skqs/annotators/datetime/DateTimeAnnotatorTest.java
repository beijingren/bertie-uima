package eu.skqs.annotators.datetime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import eu.skqu.datetime.Dynasty;


public class DateTimeAnnotatorTest {

	@Test
	public void testDynasties() throws Exception {
		JCas aJCas = JCasFactory.createJCas();

		aJCas.setDocumentText("唐朝全盛時");

		AnalysisEngine datetimeAnnotatorAE = AnalysisEngineFactory
		    .createEngine(DateTimeAnalysisEngine.class);

		datetimeAnnotatorAE.process(aJCas);

		assertEquals(1, JCasUtil.select(aJCas, Dynasty.class).size());
	}
}
