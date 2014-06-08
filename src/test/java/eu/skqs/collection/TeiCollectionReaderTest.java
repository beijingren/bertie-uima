package eu.skqs.collection;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.test.junit_extension.JUnitExtension;

import org.junit.Test;

import eu.skqs.collection.TeiCollectionReader;
import eu.skqs.annotators.datetime.DateTimeAnalysisEngine;


public class TeiCollectionReaderTest {

	@Test
	public void testTeiCollectionReader() throws Exception {

		CollectionReaderDescription reader =
		    CollectionReaderFactory.createReaderDescription(
		    TeiCollectionReader.class,
		    TeiCollectionReader.PARAM_INPUTDIR,
		    JUnitExtension.getURL("texts/TeiCollectionReader").getPath());

		SimplePipeline.runPipeline(reader);
	}
}
