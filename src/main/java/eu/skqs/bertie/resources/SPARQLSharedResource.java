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

import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

import org.apache.uima.resource.SharedResourceObject;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.DataResource;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Literal;

import eu.skqs.bertie.resources.Sparql;


public final class SPARQLSharedResource implements SharedResourceObject {

	private String mOWLFile;

	// Resources
	private Vector mTerms = new Vector();

	// TODO: remove this already in SPARQL
	private int prefixLength = "http://example.org/owl/sikuquanshu#".length();

	String termQuery =
	    "PREFIX : <http://example.org/owl/sikuquanshu#>\n" +
	    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
	    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
	    "SELECT DISTINCT ?subject WHERE {\n" +
	    "    ?class rdfs:subClassOf* :Term .\n" +
	    "    ?subject rdf:type ?class .\n" +
	    "}";

	private static Map<String, Integer> mSexagenaryCyclesMap = new HashMap<String, Integer>();
	private static Map<String, Map<String, Integer>> mEraNamesMap = new HashMap<String, Map<String, Integer>>();
	private static Map<String, Integer> mEraNamesMap1= new HashMap<String, Integer>();

	private void loadSexagenaryCycles() {
		String sexagenaryCyclesQuery =
		    "PREFIX : <http://example.org/owl/sikuquanshu#>\n" +
		    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
		    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
		    "SELECT DISTINCT (strafter(str(?subject), str(:)) AS ?cycle) (str(?object) AS ?value)\n" +
		    "{\n" +
		    "    ?subject rdf:type :SexagenaryCycle ;\n" +
		    "    :value ?object .\n" +
		    "}";

		ResultSet rs = null;
		try {
			rs = Sparql.loadQuery(mOWLFile, sexagenaryCyclesQuery);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (; rs.hasNext(); ) {
			QuerySolution rb = rs.nextSolution();

			RDFNode x = rb.get("cycle");
			Literal cycle = (Literal)x;

			RDFNode y = rb.get("value");
			Literal value = (Literal)y;

			mSexagenaryCyclesMap.put(cycle.getString(), value.getInt());
		}
	}

	private void loadEraNames() throws ResourceInitializationException {
		String eraNamesQuery =
		    "PREFIX : <http://example.org/owl/sikuquanshu#>\n" +
		    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
		    "SELECT DISTINCT " +
		    "(strafter(str(?subject), str(:)) AS ?nianhao) " +
		    "(str(?object1) AS ?beginYear) " +
		    "(str(?object2) AS ?endYear) " +
		    "{\n" +
		    "    ?subject rdf:type :Nianhao ;\n" +
		    "    :beginYear ?object1 ;\n" +
		    "    :endYear ?object2 .\n" +
		    "}";

		ResultSet rs = null;
		try {
			rs = Sparql.loadQuery(mOWLFile, eraNamesQuery);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResourceInitializationException();
		}

		for (; rs.hasNext(); ) {
			QuerySolution rb = rs.nextSolution();

			RDFNode x = rb.get("nianhao");
			Literal nianhao = (Literal)x;

			RDFNode y = rb.get("beginYear");
			Literal beginYear = (Literal)y;

			RDFNode z = rb.get("endYear");
			Literal endYear = (Literal)z;

			Map<String, Integer> eraMap = new HashMap<String, Integer>();
			eraMap.put("beginYear", beginYear.getInt());
			eraMap.put("endYear", endYear.getInt());

			mEraNamesMap.put(nianhao.getString(), eraMap);
		}
	}

	public void load(DataResource data) throws ResourceInitializationException {
		mOWLFile = data.getUri().toString();

		loadSexagenaryCycles();

		try {
			loadEraNames();
		} catch (ResourceInitializationException e) {
			throw new ResourceInitializationException();
		}

		ResultSet rs = null;
		try {
			rs = Sparql.loadQuery(mOWLFile, termQuery);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (; rs.hasNext(); ) {
			QuerySolution rb = rs.nextSolution();

			RDFNode x = rb.get("subject");

			if (x.isLiteral()) {
				Literal subjectStr = (Literal)x;
			} else {
			}

			String term = x.toString().substring(prefixLength);

			if (term.length() > 1) {
				mTerms.add(term);
			}
		}
	}

	public Vector getTerms() { return mTerms; }

	public static Map<String, Integer> getSexagenaryCycles() { return mSexagenaryCyclesMap; }
	public static Map getEraNames() { return mEraNamesMap; }
}
