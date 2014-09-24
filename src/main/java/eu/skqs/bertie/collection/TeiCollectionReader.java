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

package eu.skqs.bertie.collection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import org.xml.sax.SAXException;

import eu.skqs.cas.TeiDeserializer;
import eu.skqs.type.SourceDocumentInformation;


public class TeiCollectionReader extends CollectionReader_ImplBase {

	// Logger
	private Logger logger;

	/**
	 * Name of configuration parameter that must be set to the path of a directory containing input
	 * files.
	 */
	public static final String PARAM_INPUTDIR = "InputDirectory";

	private ArrayList<File> mFiles;

	private int mCurrentIndex;

	@Override
	public void initialize() throws ResourceInitializationException {
		super.initialize();

		logger = getUimaContext().getLogger();

		File directory = new File(((String) getConfigParameterValue(PARAM_INPUTDIR)).trim());

		// Verify path exists and is a diretory
		if (!directory.exists() || !directory.isDirectory()) {
			throw new ResourceInitializationException(
			    ResourceConfigurationException.DIRECTORY_NOT_FOUND,
			    new Object[] { PARAM_INPUTDIR, this.getMetaData().getName(),
			    directory.getPath() });
		}

		mCurrentIndex = 0;

		mFiles = new ArrayList<File>();
		addFilesFromDirectory(directory);
	}

	/**
	 * Private method to read files from a directory.
	 */
	private void addFilesFromDirectory(File directory) {
		File [] files = directory.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (!files[i].isDirectory()) {
				if (files[i].getName().endsWith(".xml")) {
					mFiles.add(files[i]);
				}
			} else {
				addFilesFromDirectory(files[i]);
			}
		}
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return mCurrentIndex < mFiles.size();
	}

	@Override
	public void getNext(CAS aCAS) throws IOException, CollectionException {

		JCas jcas = null;
		try {
			jcas = aCAS.getJCas();
		} catch (CASException e) {
			throw new CollectionException();
		}

		File currentFile = (File) mFiles.get(mCurrentIndex++);
		FileInputStream inputStream = new FileInputStream(currentFile);

		SourceDocumentInformation annotation = new SourceDocumentInformation(jcas);
		// TODO
		annotation.setUri(currentFile.getAbsoluteFile().toString());
		annotation.addToIndexes();

		logger.log(Level.INFO, currentFile.getAbsoluteFile().toString());

		try {
			TeiDeserializer.deserialize(inputStream, aCAS);
		} catch (SAXException e) {
			throw new CollectionException(e);
		} finally {
			inputStream.close();
		}
	}

	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(mCurrentIndex,
		    mFiles.size(), Progress.ENTITIES) };
	}

	@Override
	public void close() throws IOException {
	}
}
