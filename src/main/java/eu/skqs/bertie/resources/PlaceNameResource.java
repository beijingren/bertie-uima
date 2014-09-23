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

import org.apache.uima.resource.SharedResourceObject;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.DataResource;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Literal;

import eu.skqs.bertie.resources.Sparql;


public class PlaceNameResource implements SharedResourceObject {
	private String mPlaceNamePattern;

	private Vector mPlaceNames = new Vector();

	private int prefixLength = "http://example.org/owl/sikuquanshu#".length();

	private String placeNameQuery =
	    "PREFIX : <http://example.org/owl/sikuquanshu#>\n" +
	    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
	    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
	    "SELECT DISTINCT ?subject WHERE {\n" +
	    "    ?class rdfs:subClassOf* :Place  .\n" +
	    "    ?subject rdf:type ?class .\n" +
	    "}";

	public void load(DataResource data) throws ResourceInitializationException {
		String rdfFile = data.getUri().toString();

		ResultSet rs = null;
		try {
			rs = Sparql.loadQuery(rdfFile, placeNameQuery);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("EXCEPTION!!!!");
		}


		for (; rs.hasNext(); ) {
			QuerySolution rb = rs.nextSolution();

			RDFNode x = rb.get("subject");

			if (x.isLiteral()) {
				Literal subjectStr = (Literal)x;
			} else {
			}

			String placeName = x.toString().substring(prefixLength);

			// Single character names are in general too common,
			// skip them for now
			if (placeName.length() > 1) {
				mPlaceNames.add(placeName);
			}
		}
	}

	public Vector getPlaceNames() { return mPlaceNames; }
}
