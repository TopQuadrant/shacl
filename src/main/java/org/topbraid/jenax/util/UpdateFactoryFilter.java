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
