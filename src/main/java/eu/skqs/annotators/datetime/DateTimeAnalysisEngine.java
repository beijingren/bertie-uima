package eu.skqs.annotators.datetime;

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

import eu.skqs.type.Dynasty;


public class DateTimeAnalysisEngine extends JCasAnnotator_ImplBase {

	// Dynasties
	private Pattern mDynastiesPattern;
	private HashMap<String, String> mDynasties;

	// Logger
	private Logger logger;

	// Annotation
	private int totalDynasties = 0;

	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);

		mDynasties = new HashMap<String, String>();
		mDynasties.put("三國", "XXX");
		mDynasties.put("隋", "XXX");
		mDynasties.put("唐", "XXX");
		mDynasties.put("五代", "XXX");
		mDynasties.put("後蜀", "XXX");
		mDynasties.put("吳", "XXX");
		mDynasties.put("南唐", "XXX");
		mDynasties.put("吳越", "XXX");
		mDynasties.put("宋", "XXX");
		mDynasties.put("遼", "XXX");
		mDynasties.put("金", "XXX");
		mDynasties.put("元", "XXX");
		mDynasties.put("明", "XXX");
		mDynasties.put("清", "XXX");
		mDynasties.put("國朝", "XXX");

		mDynastiesPattern = Pattern.compile(Joiner.on("|").join(mDynasties.keySet()));

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
