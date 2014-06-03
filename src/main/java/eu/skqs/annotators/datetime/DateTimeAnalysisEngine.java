package eu.skqs.annotators.datetime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

import eu.skqs.type.Dynasty;


public class DateTimeAnalysisEngine extends JCasAnnotator_ImplBase {

	// Dynasties
	private Pattern mDynastiesPattern = Pattern.compile("唐");
	private int totalDynasties = 0;

	// Logger
	private Logger logger;

	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);

		logger = getContext().getLogger();
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		// Get document text
		String docText = aJCas.getDocumentText();

		int pos = 0;

		// Dynasties
		Matcher matcher = mDynastiesPattern.matcher(docText);
		while (matcher.find(pos)) {

			// Found match
			Dynasty annotation = new Dynasty(aJCas, matcher.start(), matcher.end());

			annotation.addToIndexes();

			totalDynasties++;

			logger.log(Level.FINEST, "Found: " + annotation);

			pos = matcher.end();
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		System.out.println("Total dynasties: " + totalDynasties);
	}
}
