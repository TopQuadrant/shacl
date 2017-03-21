package org.topbraid.spin.query;

import org.apache.jena.query.Syntax ;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateFactoryFilter {
	private Logger logger;
	private static UpdateFactoryFilter singleton = new UpdateFactoryFilter();
	
	/**
	 * Gets the singleton instance of this class.
	 * @return the singleton
	 */
	public static UpdateFactoryFilter get() {
		return singleton;
	}
	
	private UpdateFactoryFilter() {
		logger = LoggerFactory.getLogger(QueryExecutionFactoryFilter.LOG_NAME);
	}

	public UpdateRequest create(String str) {
		analyzeRequest(str);
		return UpdateFactory.create(str, Syntax.syntaxARQ);
	}
	
	private void analyzeRequest(String update) {
		if(logger.isTraceEnabled()) {	
			logger.trace("QUERY[" + analyzeUpdate(update));
		}
	}

	private String analyzeUpdate(String update) {
		if(update == null) return "null update query";
		return update;
	}	
	

}
