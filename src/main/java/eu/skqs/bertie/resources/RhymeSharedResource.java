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

package eu.skqs.bertie.resources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.List;

import org.apache.uima.resource.SharedResourceObject;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.DataResource;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Literal;

import eu.skqs.bertie.resources.Sparql;


public final class RhymeSharedResource implements SharedResourceObject {

	private String mOWLFile;

	// Resources
	private static Map<String, List<String>> mRimesMap = new HashMap<String, List<String>>();

	private String getSparqlQuery(String sparqlFile) {

		InputStream inputStream = SPARQLSharedResource.class.getClass().getResourceAsStream(
		    "/sparql/" + sparqlFile);

		if (inputStream == null) {
			return null;
		}

		StringBuilder stringBuilder = new StringBuilder();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

		String read = null;
		try {
			read = bufferedReader.readLine();

			while (read != null) {
				stringBuilder.append(read);
				read = bufferedReader.readLine();
			}

		} catch (IOException e) {
			return null;
		}

		return stringBuilder.toString();
	}

	private void loadRimes() throws ResourceInitializationException {
		String query = getSparqlQuery("shared_resource_rime.sparql");

		ResultSet rs = null;
		try {
			rs = Sparql.loadQuery(mOWLFile, query);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResourceInitializationException(e);
		}

		for (; rs.hasNext(); ) {
			QuerySolution rb = rs.nextSolution();

			RDFNode x = rb.get("glyph");
			Literal glyph = (Literal)x;

			RDFNode y = rb.get("rime");
			Literal rime = (Literal)y;

			RDFNode z = rb.get("tone");
			Literal tone = (Literal)z;

			List rimeList = new Vector<String>();
			rimeList.add(rime.getString());
			rimeList.add(tone.getString());

			mRimesMap.put(glyph.getString(), rimeList);
		}
	}

	public void load(DataResource data) throws ResourceInitializationException {
		mOWLFile = data.getUri().toString();

		// Rimes
		try {
			loadRimes();
		} catch (ResourceInitializationException e) {
			throw new ResourceInitializationException(e);
		}

	}

	public static Map<String, List<String>> getRimes() { return mRimesMap; }
}
