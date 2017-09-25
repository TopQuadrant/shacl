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
package org.topbraid.spin.util;

import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.spin.model.Command;


/**
 * A CommandWrapper that wraps a SPARQL query 
 * (in contrast to UpdateWrapper for UPDATE requests).
 * 
 * @author Holger Knublauch
 */
public class QueryWrapper extends CommandWrapper {
	
	private Query query;
	
	private org.topbraid.spin.model.Query spinQuery;
	
	
	public QueryWrapper(Query query, Resource source, String text, org.topbraid.spin.model.Query spinQuery, String label, Statement statement, boolean thisUnbound, boolean thisDeep) {
		super(source, text, label, statement, thisUnbound, thisDeep);
		this.query = query;
		this.spinQuery = spinQuery;
	}
	
	
	public Query getQuery() {
		return query;
	}
	
	
	@Override
	public Command getSPINCommand() {
		return getSPINQuery();
	}


	public org.topbraid.spin.model.Query getSPINQuery() {
		return spinQuery;
	}
}
