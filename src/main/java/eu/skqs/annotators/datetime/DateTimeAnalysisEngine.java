package eu.skqs.annotators.datetime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.fit.descriptor.ConfigurationParameter;

import eu.skqu.datetime.Dynasty;


public class DateTimeAnalysisEngine extends JCasAnnotator_ImplBase {

	// Dynasties
	private Pattern mDynastiesPattern = Pattern.compile("唐");

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		// Get document text
		String docText = aJCas.getDocumentText();

		int pos = 0;

		// Dynasties
		Matcher matcher = mDynastiesPattern.matcher(docText);
		while (matcher.find(pos)) {

			// Found match
			Dynasty annotation = new Dynasty(aJCas);
			annotation.setBegin(matcher.start());
			annotation.setEnd(matcher.end());
			annotation.addToIndexes();

			pos = matcher.end();
		}
	}
}
