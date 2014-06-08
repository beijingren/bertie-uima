package eu.skqs.cas;

import java.io.InputStream;
import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.tcas.Annotation;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.SAXException;

import eu.skqs.type.Dynasty;
import eu.skqs.type.Interpunction;


public class TeiDeserializer {

	public static void deserialize(InputStream aStream, CAS aCAS) throws SAXException, IOException {
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();

		ContentHandler handler = new TeiDeserializerHandler(aCAS);
		xmlReader.setContentHandler(handler);
		xmlReader.parse(new InputSource(aStream));
	}

	static class TeiDeserializerHandler extends DefaultHandler {
		TeiDeserializerHandler(CAS aCAS) {
		}
	}
}
