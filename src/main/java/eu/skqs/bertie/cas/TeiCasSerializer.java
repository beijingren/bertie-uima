package eu.skqs.bertie.cas;

import java.io.OutputStream;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.cas.Type;

import org.apache.uima.internal.util.XmlElementName;

import org.xml.sax.ContentHandler;

import eu.skqs.type.Dynasty;
import eu.skqs.type.Interpunction;


public class TeiCasSerializer {

	// TODO
	public TeiCasSerializer() {
	}

	public TeiCasSerializer(TypeSystem aTypeSystem) {
	}

	public static String serialize(JCas aJCas, ContentHandler contentHandler) {

		String documentText = aJCas.getDocumentText();
		String result = "";
		int startPosition = 0;
		int endPosition = documentText.length();

		FSIterator annotationIterator = aJCas.getAnnotationIndex().iterator();

		while (annotationIterator.hasNext()) {
			Annotation annotation = (Annotation)annotationIterator.next();

			String annotationName = annotation.getType().getName();
			String tagName = null;

			if (annotationName.equals("eu.skqs.type.Interpunction")) {
				tagName = "pc";
				endPosition = annotation.getBegin();

				result += documentText.substring(startPosition, endPosition);
				result += "\n";
				result += "<" + tagName + ">";
				result += annotation.getCoveredText();
				result += "</" + tagName + ">";
				result += "\n";

				startPosition = annotation.getEnd();
			} else if (annotationName.equals("eu.skqs.type.Num")) {
				tagName = "num";
				endPosition = annotation.getBegin();

				result += documentText.substring(startPosition, endPosition);
				result += "\n";
				result += "<" + tagName + ">";
				result += annotation.getCoveredText();
				result += "</" + tagName + ">";
				result += "\n";

				startPosition = annotation.getEnd();
			} else if (annotationName.equals("eu.skqs.type.PersName")) {
				tagName = "persName";
				endPosition = annotation.getBegin();

				result += documentText.substring(startPosition, endPosition);
				result += "\n";
				result += "<" + tagName + ">";
				result += annotation.getCoveredText();
				result += "</" + tagName + ">";
				result += "\n";

				startPosition = annotation.getEnd();
			}

		}

		return result;
	}
}
