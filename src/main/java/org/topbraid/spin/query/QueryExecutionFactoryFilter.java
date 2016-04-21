package org.topbraid.spin.query;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;

public class QueryExecutionFactoryFilter {
	static final String LOG_NAME = "QueryLog";
	private Logger logger;
	private static QueryExecutionFactoryFilter singleton = new QueryExecutionFactoryFilter();
	
	/**
	 * Gets the singleton instance of this class.
	 * @return the singleton
	 */
	public static QueryExecutionFactoryFilter get() {
		return singleton;
	}
	
	private QueryExecutionFactoryFilter() {
		logger = Logger.getLogger(LOG_NAME);
	}

	public QueryExecution create(Query query, Model model) {
		analyzeRequest(query, model, null);
		return QueryExecutionFactory.create(query, model);
	}

	public QueryExecution create(Query query, Model model, QuerySolution initialBinding) {
		analyzeRequest(query, model, initialBinding);
		return QueryExecutionFactory.create(query, model, initialBinding);
	}

	public QueryExecution create(Query query, Dataset dataset) {
		analyzeRequest(query, dataset, null);
		return QueryExecutionFactory.create(query, dataset);
	}

	public QueryExecution create(Query query, Dataset dataset, QuerySolution initialBinding) {
		analyzeRequest(query, dataset, initialBinding);
		return QueryExecutionFactory.create(query, dataset, initialBinding);
	}

	public QueryExecution sparqlService(String service, Query query) {
		return QueryExecutionFactory.sparqlService(service, query);
	}
	
	
	
	
	
	private void analyzeRequest(Query query, Model model, QuerySolution initialBinding) {
		if(logger.isTraceEnabled()) {	
			logger.trace("QUERY[" + analyzeQuery(query) 
				+ "]\nMODEL[" + analyzeModel(model) + "]" 
				+  serializeBindings(initialBinding));
		}
	}
	
	private void analyzeRequest(Query query, Dataset dataset, QuerySolution initialBinding) {
		if(logger.isTraceEnabled()) {	
			logger.trace("QUERY[" + analyzeQuery(query) 
				+ "]\nDATASET[" + analyzeDataset(dataset) + "]" 
				+  serializeBindings(initialBinding));
		}
	}
		
	private String serializeBindings(QuerySolution bindings) {
		if(bindings == null) return "";
		return "\nINITIAL BINDINGS[" + bindings.toString() + "]";
	}
	
	private String analyzeQuery(Query query) {
		if(query == null) return "null query";
		return query.toString();
	}
	
	private String analyzeModel(Model model) {
		if(model == null) return "null model";
		
		return "this space for rent";
	}
	
	private String analyzeDataset(Dataset dataset) {
		if(dataset == null) return "null dataset";
		
		return "A Dataset";
	}
	

}
