package eu.skqs.cas;

import java.io.OutputStream;

import org.apache.uima.jcas.JCas;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;

import org.xml.sax.ContentHandler;

import eu.skqs.type.Dynasty;


public class TeiCasSerializer {

	public TeiCasSerializer(TypeSystem aTypeSystem) {
	}

	public static void serialize(JCas aJCas, ContentHandler contentHandler) {

		String documentText = aJCas.getDocumentText();
		int startPosition = 0;
		int endPosition = documentText.length();

		FSIndex dynastyIndex = aJCas.getAnnotationIndex(Dynasty.type);
		FSIterator dynastyIterator = dynastyIndex.iterator();

		while (dynastyIterator.hasNext()) {

			Dynasty dynasty = (Dynasty)dynastyIterator.next();

			System.out.print("<date>");
			System.out.print(dynasty.getCoveredText());
			System.out.println("</date>");
		}
	}
}
