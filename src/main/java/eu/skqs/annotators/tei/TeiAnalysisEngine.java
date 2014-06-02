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
