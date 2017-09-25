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
package org.topbraid.spin.model;


/**
 * An abstraction for Query, Modify and DeleteWhere, i.e. all SPARQL commands
 * that may contain a WHERE clause.
 * 
 * @author Holger Knublauch
 */
public interface CommandWithWhere extends Command {
	
	/**
	 * Gets the ElementList of the WHERE clause of this query.
	 * Might be null or RDF.nil.
	 * @return the WHERE clause as an ElementList
	 */
	ElementList getWhere();
}
