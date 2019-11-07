package org.topbraid.shacl.templates;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.validation.sparql.SPARQLSubstitutions;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Support for executing SHACL Query Templates, for now dash:SPARQLConstructTemplates and dash:SPARQLSelectTemplates.
 * 
 * See http://datashapes.org/templates.html for background.
 * 
 * @author Holger Knublauch
 */
public class Templates {
	
	/**
	 * Takes an instance of dash:SPARQLConstructTemplate and parameter bindings and returns a Model with the triples
	 * that result from the execution of all CONSTRUCT queries in the template using the given parameter bindings.
	 * @param template  the template defining the sh:construct queries to run
	 * @param bindings  the initial bindings for the CONSTRUCT queries
	 * @param dataset  the Dataset to query over
	 * @return a Model with the constructed triples
	 */
	public static Model construct(Resource template, QuerySolutionMap bindings, Dataset dataset) {
		Model result = JenaUtil.createDefaultModel();
		template.listProperties(SH.construct).filterKeep(s -> s.getObject().isLiteral()).forEachRemaining(s -> {			
			String queryString = s.getString();
			Query arqQuery = ARQFactory.get().createQuery(SPARQLSubstitutions.withPrefixes(queryString, template));
			try(QueryExecution qexec = ARQFactory.get().createQueryExecution(arqQuery, dataset, bindings)) {
				qexec.execConstruct(result);
			}
		});
		return result;
	}

	
	/**
	 * Takes an instance of dash:SPARQLSelectTemplate and parameter bindings and returns a QueryExecution object for the
	 * sh:select query in the template using the given parameter bindings.
	 * @param template  the template defining the sh:select query to run
	 * @param bindings  the initial bindings for the SELECT query
	 * @param dataset  the Dataset to query over
	 * @return a QueryExecution
	 */
	public static QueryExecution select(Resource template, QuerySolutionMap bindings, Dataset dataset) {
		String queryString = JenaUtil.getStringProperty(template, SH.select);
		Query arqQuery = ARQFactory.get().createQuery(SPARQLSubstitutions.withPrefixes(queryString, template));
		return ARQFactory.get().createQueryExecution(arqQuery, dataset, bindings);
	}
}
