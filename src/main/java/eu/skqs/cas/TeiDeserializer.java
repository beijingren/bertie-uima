package eu.skqs.cas;

import java.io.InputStream;
import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import org.xml.sax.Attributes;
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

		JCas aJCas = null;

		try {
			aJCas = aCAS.getJCas();
		} catch (CASException e) {
			throw new IOException(e);
		}

		XMLReader xmlReader = XMLReaderFactory.createXMLReader();

		ContentHandler handler = new TeiDeserializerHandler(aJCas);
		xmlReader.setContentHandler(handler);
		xmlReader.parse(new InputSource(aStream));
	}

	static class TeiDeserializerHandler extends DefaultHandler {

		private static final String TAG_DATE = "date";
		private static final String TAG_PC = "pc";
		private static final String TAG_PERSNAME = "persName";
		private static final String TAG_PLACENAME = "placeName";

		private boolean captureText = false;

		private JCas mJCas;

		private StringBuffer buffer = new StringBuffer();
		private int tagStart = 0;

		TeiDeserializerHandler(JCas aJCas) {
			mJCas = aJCas;
		}

		@Override
		public void startElement(String aUri, String aLocalName, String qName,
		    Attributes aAttributes) throws SAXException {

			System.out.println("startElement " + qName);

			if (TAG_DATE.equals(qName)) {
				captureText = true;
				// tokenStart = getBuffer.length();
			} else if (TAG_PC.equals(qName)) {
				captureText = true;
			}

			tagStart = buffer.length();
		}

		@Override
		public void endElement(String aUri, String aLocalName, String qName)
		    throws SAXException {

			System.out.println("endElement " + qName);

			if (TAG_PC.equals(qName)) {
				Interpunction pc = new Interpunction(mJCas);
				pc.setBegin(tagStart);
				pc.setEnd(buffer.length());
				pc.addToIndexes();
			}
		}

		@Override
		public void characters(char[] aChar, int aStart, int aLength)
		    throws SAXException {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append(aChar, aStart, aLength);

			buffer.append(stringBuffer.toString());

			System.out.println(stringBuffer.toString());
		}
	}
}
