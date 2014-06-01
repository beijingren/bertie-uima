package eu.skqs.annotators.datetime;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.xml.sax.SAXException;


public class DateTimeDescriptor {

	public static void main(String[] args) throws ResourceInitializationException,
	    FileNotFoundException, SAXException, IOException {

		AnalysisEngineDescription analysisEngineDescription = AnalysisEngineFactory
		    .createPrimitiveDescription(DateTimeAnalysisEngine.class);

		analysisEngineDescription.toXML(new FileOutputStream("DateTimeAnalysisEngine.xml"));
	}
}

