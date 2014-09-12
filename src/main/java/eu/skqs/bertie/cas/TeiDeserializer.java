package eu.skqs.cas;

import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;

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

import eu.skqs.type.Body;
import eu.skqs.type.Date;
import eu.skqs.type.DateRange;
import eu.skqs.type.Div;
import eu.skqs.type.P;
import eu.skqs.type.Pc;
import eu.skqs.type.PersName;
import eu.skqs.type.PlaceName;
import eu.skqs.type.Text;
import eu.skqs.type.Title;
import eu.skqs.type.Tei;


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

		private static final String TAG_AUTHOR = "author";
		private static final String TAG_BODY = "body";
		private static final String TAG_DATE = "date";
		private static final String TAG_DATERANGE = "dateRange";
		private static final String TAG_DIV = "div";
		private static final String TAG_P = "p";
		private static final String TAG_PC = "pc";
		private static final String TAG_PERSNAME = "persName";
		private static final String TAG_PLACENAME = "placeName";
		private static final String TAG_SIC = "sic";
		private static final String TAG_TEXT = "text";
		private static final String TAG_TITLE = "title";
		private static final String TAG_TITLESTMT = "titleStmt";
		private static final String TAG_TEI = "TEI";

		private boolean captureText = false;

		private JCas mJCas;

		private StringBuffer buffer = new StringBuffer();
		private int tagStart = 0;

		private HashMap<String, Integer> mPositions = new HashMap<String, Integer>();

		TeiDeserializerHandler(JCas aJCas) {
			mJCas = aJCas;
		}

		@Override
		public void endDocument() throws SAXException {
			mJCas.setDocumentText(buffer.toString());
			mJCas.setDocumentLanguage("zh");
		}

		@Override
		public void startElement(String aUri, String aLocalName, String qName,
		    Attributes aAttributes) throws SAXException {

			if (TAG_DATE.equals(qName)) {
				captureText = true;
				// tokenStart = getBuffer.length();
			} else if (TAG_PC.equals(qName)) {
				captureText = true;
			}

			tagStart = buffer.length();
			mPositions.put(qName, buffer.length());
		}

		@Override
		public void endElement(String aUri, String aLocalName, String qName)
		    throws SAXException {

			if (TAG_PC.equals(qName)) {
				Pc pc = new Pc(mJCas);

				pc.setBegin(mPositions.get(TAG_PC));
				pc.setEnd(buffer.length());

				pc.addToIndexes();
			} else if (TAG_DATE.equals(qName)) {
				// Date date = new Date(mJCas);
			} else if (TAG_DATERANGE.equals(qName)) {
				DateRange dateRange = new DateRange(mJCas);

				dateRange.setBegin(mPositions.get(TAG_DATERANGE));
				dateRange.setEnd(buffer.length());

				dateRange.addToIndexes();
			} else if (TAG_BODY.equals(qName)) {
				Body body = new Body(mJCas);

				body.setBegin(mPositions.get(TAG_BODY));
				body.setEnd(buffer.length());

				body.addToIndexes();
			} else if (TAG_TEXT.equals(qName)) {
				Text text = new Text(mJCas);

				text.setBegin(mPositions.get(TAG_TEXT));
				text.setEnd(buffer.length());

				text.addToIndexes();
			} else if (TAG_DIV.equals(qName)) {
				Div div = new Div(mJCas);

				div.setBegin(mPositions.get(TAG_DIV));
				div.setEnd(buffer.length());

				div.addToIndexes();
			} else if (TAG_P.equals(qName)) {
				P p = new P(mJCas);

				p.setBegin(mPositions.get(TAG_P));
				p.setEnd(buffer.length());

				p.addToIndexes();
			} else if (TAG_PERSNAME.equals(qName)) {
				PersName persName = new PersName(mJCas);

				persName.setBegin(mPositions.get(TAG_PERSNAME));
				persName.setEnd(buffer.length());

				persName.addToIndexes();
			} else if (TAG_TITLE.equals(qName)) {
				Title title = new Title(mJCas);

				title.setBegin(mPositions.get(TAG_TITLE));
				title.setEnd(buffer.length());

				title.addToIndexes();
			} else if (TAG_TEI.equals(qName)) {
				Tei tei = new Tei(mJCas);

				tei.setBegin(mPositions.get(TAG_TEI));
				tei.setEnd(buffer.length());

				tei.setTitle("XXX");
				tei.setTitleEn("XXX");
				tei.setAuthor("XXX");
				tei.addToIndexes();
			}
		}

		@Override
		public void characters(char[] aChar, int aStart, int aLength)
		    throws SAXException {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append(aChar, aStart, aLength);

			buffer.append(stringBuffer.toString().trim());
		}
	}
}
