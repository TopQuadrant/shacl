package org.topbraid.spin.query;

import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.log4j.Logger;

public class UpdateFactoryFilter {
	static final String LOG_NAME = "QueryLog";
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
		logger = Logger.getLogger(LOG_NAME);
	}

	public UpdateRequest create(String str) {
		analyzeRequest(str);
		return UpdateFactory.create(str);
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
