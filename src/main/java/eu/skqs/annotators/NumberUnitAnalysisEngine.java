package eu.skqs.annotators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

import com.google.common.base.Joiner;

import eu.skqs.type.Interpunction;


public class NumberUnitAnalysisEngine extends JCasAnnotator_ImplBase {

	// Interpuction
	private Pattern mNumeralsPattern;

	// Logger
	private Logger logger;

	// Annotation
	private int totalNumerals = 0;

	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);

		mNumeralsPattern = Pattern.compile("[一二三四五六七八九十]+");

		logger = getContext().getLogger();
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		// Get document text
		String docText = aJCas.getDocumentText();

		int pos = 0;

		// Numerals
		Matcher matcher = mNumeralsPattern.matcher(docText);
		while (matcher.find(pos)) {

			// Found match
			Interpunction annotation = new Interpunction(aJCas, matcher.start(), matcher.end());

			annotation.addToIndexes();

			totalNumerals++;

			logger.log(Level.FINEST, "Found: " + annotation);

			pos = matcher.end();
		}

		System.out.println("Num: " + totalNumerals);

	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		System.out.println("Total interpunction: " + totalNumerals);
	}
}
