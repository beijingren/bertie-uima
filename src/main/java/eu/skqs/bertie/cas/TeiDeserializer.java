/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package eu.skqs.cas;

import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

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
import eu.skqs.type.Measure;
import eu.skqs.type.Num;
import eu.skqs.type.P;
import eu.skqs.type.Pc;
import eu.skqs.type.PersName;
import eu.skqs.type.PlaceName;
import eu.skqs.type.Tei;
import eu.skqs.type.Text;
import eu.skqs.type.Time;
import eu.skqs.type.Title;


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
		private static final String TAG_MEASURE = "measure";
		private static final String TAG_NUM = "num";
		private static final String TAG_P = "p";
		private static final String TAG_PC = "pc";
		private static final String TAG_PERSNAME = "persName";
		private static final String TAG_PLACENAME = "placeName";
		private static final String TAG_SIC = "sic";
		private static final String TAG_TEI = "TEI";
		private static final String TAG_TEXT = "text";
		private static final String TAG_TITLE = "title";
		private static final String TAG_TIME = "time";
		private static final String TAG_TITLESTMT = "titleStmt";
		private static final String TAG_TEIHEADER = "teiHeader";

		private boolean captureText = false;
		private boolean mTitleStmt = false;
		private boolean mTeiHeader = false;

		private String mTitleLang = null;

		private JCas mJCas;

		private Stack mNumStack = new Stack();
		private Stack mMeasureStack = new Stack();
		private Stack mTeiStack = new Stack();
		private Stack mPcStack = new Stack();
		private Stack mDivStack = new Stack();

		private StringBuffer buffer = new StringBuffer();
		private int tagStart = 0;
		private int tokenStart = 0;

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

			if (TAG_TITLE.equals(qName) && mTeiHeader) {
				captureText = true;

				mTitleLang = aAttributes.getValue("xml:lang");
				tokenStart = buffer.length();
			} else if (TAG_AUTHOR.equals(qName) && mTeiHeader) {
				captureText = true;
				tokenStart = buffer.length();
			} else if (TAG_P.equals(qName) && mTeiHeader) {
				tokenStart = buffer.length();
			} else if (TAG_PC.equals(qName)) {
				captureText = true;
			}

			// title tag can occur in titlestmt and in body
			if (TAG_TITLESTMT.equals(qName)) {
				mTitleStmt = true;
			}
			if (TAG_TEIHEADER.equals(qName)) {
				mTeiHeader = true;
			} else if (TAG_TEI.equals(qName)) {
				Tei annotation = new Tei(mJCas);

				annotation.setBegin(buffer.length());

				mTeiStack.push(annotation);
			}

			if (TAG_NUM.equals(qName)) {
				Num annotation = new Num(mJCas);

				annotation.setBegin(buffer.length());

				String value = aAttributes.getValue("value");
				annotation.setValue(Integer.parseInt(value));

				mNumStack.push(annotation);
			} else if (TAG_MEASURE.equals(qName)) {
				Measure annotation = new Measure(mJCas);

				annotation.setBegin(buffer.length());

				String quantity = aAttributes.getValue("quantity");
				annotation.setQuantity(Integer.parseInt(quantity));

				String unit = aAttributes.getValue("unit");
				annotation.setUnit(unit);

				mMeasureStack.push(annotation);
			} else if (TAG_PC.equals(qName)) {
				Pc annotation = new Pc(mJCas);

				annotation.setBegin(buffer.length());

				mPcStack.push(annotation);
			} else if (TAG_DIV.equals(qName)) {
				Div annotation = new Div(mJCas);

				annotation.setBegin(buffer.length());

				String n = aAttributes.getValue("n");
				if (n != null) {
					annotation.setN(Integer.parseInt(n));
				}

				String type = aAttributes.getValue("type");
				if (type != null) {
					annotation.setTeitype(type);
				}

				mDivStack.push(annotation);
			}

			tagStart = buffer.length();
			mPositions.put(qName, buffer.length());
		}

		@Override
		public void endElement(String aUri, String aLocalName, String qName)
		    throws SAXException {

			if (TAG_PC.equals(qName)) {
				Pc annotation = (Pc)mPcStack.pop();

				annotation.setEnd(buffer.length());

				annotation.addToIndexes();
			} else if (TAG_DATE.equals(qName)) {
				Date annotation = new Date(mJCas);

				annotation.setBegin(mPositions.get(TAG_DATE));
				annotation.setEnd(buffer.length());

				annotation.addToIndexes();
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
				Div div = (Div)mDivStack.pop();

				div.setEnd(buffer.length());

				div.addToIndexes();
			} else if (TAG_AUTHOR.equals(qName)) {
				if (mTeiHeader) {
					Tei annotate = (Tei)mTeiStack.peek();

					annotate.setAuthor(buffer.substring(tokenStart, buffer.length()));
					buffer.delete(tokenStart, buffer.length());
				}
			} else if (TAG_P.equals(qName)) {
				if (mTeiHeader) {
					buffer.delete(tokenStart, buffer.length());
				} else {
					P p = new P(mJCas);

					p.setBegin(mPositions.get(TAG_P));
					p.setEnd(buffer.length());

					p.addToIndexes();
				}
			} else if (TAG_PERSNAME.equals(qName)) {
				PersName persName = new PersName(mJCas);

				persName.setBegin(mPositions.get(TAG_PERSNAME));
				persName.setEnd(buffer.length());

				persName.addToIndexes();
			} else if (TAG_PLACENAME.equals(qName)) {
				PlaceName  annotation = new PlaceName(mJCas);

				annotation.setBegin(mPositions.get(TAG_PLACENAME));
				annotation.setEnd(buffer.length());

				annotation.addToIndexes();
			} else if (TAG_MEASURE.equals(qName)) {
				Measure annotation = (Measure)mMeasureStack.pop();

				annotation.setEnd(buffer.length());

				annotation.addToIndexes();
			} else if (TAG_NUM.equals(qName)) {
				Num annotation = (Num)mNumStack.pop();

				annotation.setEnd(buffer.length());

				annotation.addToIndexes();
			} else if (TAG_TITLE.equals(qName)) {
				if (mTeiHeader) {
					Tei tei = (Tei)mTeiStack.peek();
					if (mTitleLang.equals("zh")) {
					tei.setTitle(buffer.substring(tokenStart, buffer.length()));
					} else {
					tei.setTitleEn(buffer.substring(tokenStart, buffer.length()));
					}
					buffer.delete(tokenStart, buffer.length());
				} else {
					Title title = new Title(mJCas);

					title.setBegin(mPositions.get(TAG_TITLE));
					title.setEnd(buffer.length());

					title.addToIndexes();
				}
			} else if (TAG_TEI.equals(qName)) {
				Tei tei = (Tei)mTeiStack.pop();

				tei.setEnd(buffer.length());

				tei.addToIndexes();
			} else if (TAG_TITLESTMT.equals(qName)) {
				mTitleStmt = false;
			} else if (TAG_TEIHEADER.equals(qName)) {
				mTeiHeader = false;
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
