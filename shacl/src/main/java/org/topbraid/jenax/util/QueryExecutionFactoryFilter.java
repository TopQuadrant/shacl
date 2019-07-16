/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */
package org.topbraid.jenax.util;

import org.apache.commons.lang3.time.FastDateFormat ;
import org.apache.http.client.HttpClient ;
import org.apache.jena.atlas.lib.DateTimeUtils ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryExecutionFactoryFilter {
	static final String LOG_NAME = "QueryLog";
	private Logger logger;
	private static QueryExecutionFactoryFilter singleton = new QueryExecutionFactoryFilter();
	
	// ---- Support for controlling printing queries while running. See function "printQuery".
	private static boolean PRINT = false;
    // ---- Support for controlling printing queries while running.
	
	/**
	 * Gets the singleton instance of this class.
	 * @return the singleton
	 */
	public static QueryExecutionFactoryFilter get() {
		return singleton;
	}
	
	private QueryExecutionFactoryFilter() {
		logger = LoggerFactory.getLogger(LOG_NAME);
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
	
    public QueryExecution sparqlService(String service, Query query, HttpClient httpClient) {
        return QueryExecutionFactory.sparqlService(service, query, httpClient);
    }
    
	private void analyzeRequest(Query query, Model model, QuerySolution initialBinding) {
        printQuery(query, initialBinding);

		if(logger.isTraceEnabled()) {	
			logger.trace("QUERY[" + analyzeQuery(query) 
				+ "]\nMODEL[" + analyzeModel(model) + "]" 
				+  serializeBindings(initialBinding));
		}
	}
	
	private void analyzeRequest(Query query, Dataset dataset, QuerySolution initialBinding) {
	    printQuery(query, initialBinding);
	    
		if(logger.isTraceEnabled()) {	
			logger.trace("QUERY[" + analyzeQuery(query) 
				+ "]\nDATASET[" + analyzeDataset(dataset) + "]" 
				+  serializeBindings(initialBinding));
		}
	}
	
	private static FastDateFormat timestamp = FastDateFormat.getInstance("HH:mm:ss.SSS") ;
	// Development support. Dynmically controlled print query.
	private void printQuery(Query query, QuerySolution initialBinding) {
	    if ( PRINT ) {
	        String time = DateTimeUtils.nowAsString(timestamp); 
            System.err.print("~~ ");
            System.err.print(time);
            System.err.println(" ~~");
            System.err.println(initialBinding);
            System.err.print(query);
        }
	}

    /**
     * Allow query printing to be switched on/off around specific sections of code that
     * are issuing queries.
     * @param value  true to enable
     */
    public static void enableQueryPrinting(boolean value) {
        PRINT = value;
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
