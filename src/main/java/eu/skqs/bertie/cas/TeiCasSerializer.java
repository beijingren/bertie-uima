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

	public static void serialize(JCas aJCas, ContentHandler contentHandler) {

		String documentText = aJCas.getDocumentText();
		int startPosition = 0;
		int endPosition = documentText.length();

		FSIndex dynastyIndex = aJCas.getAnnotationIndex(Dynasty.type);
		FSIterator dynastyIterator = dynastyIndex.iterator();

		while (dynastyIterator.hasNext()) {

			Dynasty dynasty = (Dynasty)dynastyIterator.next();

			endPosition = dynasty.getBegin();

			System.out.println(documentText.substring(startPosition, endPosition));
			System.out.print("<date>");
			System.out.print(dynasty.getCoveredText());
			System.out.println("</date>");

			startPosition = dynasty.getEnd();
		}

		System.out.println(Dynasty.type);
		System.out.println(Interpunction.type);


		FSIterator annotationIterator = aJCas.getAnnotationIndex().iterator();

		while (annotationIterator.hasNext()) {
			Annotation annotation = (Annotation)annotationIterator.next();

				//Type type = annotation.getType();
				//System.out.println(annotation.getClass());
				//System.out.println(aJCas.getType(Interpunction.type));
			System.out.println(annotation.getType().getName());
		}
	}
}
