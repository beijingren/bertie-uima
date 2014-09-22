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

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.File;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;


public class Sparql {

	private static Logger logger = UIMAFramework.getLogger();

	public static ResultSet loadQuery(String rdfFile, String queryString) throws ResourceInitializationException {

		// Read RDF file
		InputStream in = null;
		try {
			in = new FileInputStream(new File(rdfFile));
		} catch (Exception e) {
			logger.log(Level.WARNING, "Can not open file: " + rdfFile);
			throw new ResourceInitializationException();
		}

		System.out.println("++++ " +rdfFile);
		try {
			System.out.println(new FileReader(new File(rdfFile)).read());
		} catch (Exception e) {
		}

		Model model = ModelFactory.createMemModelMaker().createModel("SKQS");
		model.read(in, null);

		try {
			in.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Can not close input stream " + rdfFile);
			// TODO
		}

		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);

		ResultSet rs = qe.execSelect();

		// TODO
		// qe.close();

		return rs;
	}
}
