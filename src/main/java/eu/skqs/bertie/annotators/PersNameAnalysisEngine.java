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

package eu.skqs.bertie.annotators;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Vector;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
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

import com.google.common.base.Joiner;

import eu.skqs.type.PersName;


public class PersNameAnalysisEngine extends JCasAnnotator_ImplBase {

	// Person names
	private Pattern mPersNamePattern;

	// Zi
	private Pattern mZiPattern;

	private Pattern mMaternalPattern;

	// Logger
	private Logger logger;

	// Annotation
	private int totalPersName = 0;

	// RDF
	private String rdfFile = "/docker/dublin-store/rdf/sikuquanshu.rdf";
	private int prefixLength = "http://example.org/owl/sikuquanshu#".length();

	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);

		// Logger
		logger = getContext().getLogger();

		// SPARQL
		InputStream in = null;
		try {
			in = new FileInputStream(new File(rdfFile));
		} catch (Exception e) {
			logger.log(Level.WARNING, "Could not find: " +
			     rdfFile);
			throw new ResourceInitializationException();
		}

		Model model = ModelFactory.createMemModelMaker().createModel("SKQS");
		model.read(in, null);
		try {
			in.close();
		} catch (Exception e) {
		}

		// TODO: remove prefix : from results with bind
		String queryString =
			"PREFIX : <http://example.org/owl/sikuquanshu#>\n" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"SELECT ?s WHERE { ?s rdf:type :Person . }";

		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);

		Vector namedIndividuals = new Vector();

		try {
			ResultSet rs = qe.execSelect();

			for (; rs.hasNext();) {
				QuerySolution rb = rs.nextSolution();

				RDFNode x = rb.get("s");
				if (x.isLiteral()) {
					Literal subjectStr = (Literal)x;
				} else {
				}

				String individual = x.toString().substring(prefixLength);

				// Single character names are in general too common,
				// skip them for now
				if (individual.length() > 1) {
					namedIndividuals.add(individual);
				}
			}

			// ResultSetFormatter.out(System.out, rs, query);

		} finally {
			qe.close();
		}

		mPersNamePattern = Pattern.compile(Joiner.on("|").join(namedIndividuals));

		/*
		 * Zi
		 */
		// TODO: add attribute to person who has this name
		queryString =
			"PREFIX : <http://example.org/owl/sikuquanshu#>\n" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"SELECT ?zi WHERE { ?s :zi ?zi . }";

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, model);

		Vector zis = new Vector();

		try {
			ResultSet rs = qe.execSelect();

			for (; rs.hasNext();) {
				QuerySolution rb = rs.nextSolution();

				RDFNode x = rb.get("zi");
				if (x.isLiteral()) {
					Literal subjectStr = (Literal)x;
				} else {
				}

				String zi = x.toString().substring(prefixLength);

				// Single character names are in general too common,
				// skip them for now
				if (zi.length() > 1) {
					zis.add(zi);
				}
			}
		} finally {
			qe.close();
		}

		mZiPattern = Pattern.compile(Joiner.on("|").join(zis));

		mMaternalPattern = Pattern.compile("(母|姓)(\\p{Alnum}{1,2})氏", Pattern.UNICODE_CHARACTER_CLASS);
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		// Get document text
		String docText = aJCas.getDocumentText();

		int pos = 0;

		// named Individual
		Matcher matcher = mPersNamePattern.matcher(docText);
		while (matcher.find(pos)) {

			// Found match
			PersName annotation = new PersName(aJCas, matcher.start(), matcher.end());

			annotation.addToIndexes();

			totalPersName++;

			pos = matcher.end();
		}

		pos = 0;
		matcher = mZiPattern.matcher(docText);
		while (matcher.find(pos)) {

			// Found match
			PersName annotation = new PersName(aJCas, matcher.start(), matcher.end());

			annotation.addToIndexes();

			totalPersName++;

			pos = matcher.end();
		}


		// 母鄭
		// 母杜氏
		// father and mother
		pos = 0;
		matcher = mMaternalPattern.matcher(docText);
		while (matcher.find(pos)) {
			PersName annotation = new PersName(aJCas, matcher.start(2), matcher.end(2));

			annotation.addToIndexes();

			totalPersName++;

			pos = matcher.end();
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		System.out.println("Total Individuals: " + totalPersName);
	}
}
