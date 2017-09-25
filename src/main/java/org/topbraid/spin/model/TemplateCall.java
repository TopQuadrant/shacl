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

import java.util.Map;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;


/**
 * A template call.
 * 
 * @author Holger Knublauch
 */
public interface TemplateCall extends ModuleCall {

	/**
	 * Creates a QueryExecution that can be used to execute the associated query
	 * with the correct variable bindings.
	 * @param dataset  the Dataset to operate on
	 * @return the QueryExecution
	 */
	QueryExecution createQueryExecution(Dataset dataset);

	
	/**
	 * Gets a Map from ArgumentDescriptors to RDFNodes.
	 * @return a Map from ArgumentDescriptors to RDFNodes
	 */
	Map<Argument,RDFNode> getArgumentsMap();

	
	/**
	 * Gets a Map from Properties to RDFNodes derived from the
	 * ArgumentDescriptors.
	 * @return a Map from Properties to RDFNodes
	 */
	Map<Property,RDFNode> getArgumentsMapByProperties();

	
	/**
	 * Gets a Map from variable names to RDFNodes derived from the
	 * ArgumentDescriptors.
	 * @return a Map from variable names to RDFNodes
	 */
	Map<String,RDFNode> getArgumentsMapByVarNames();
	
	
	/**
	 * Gets the name-value pairs of the template call's arguments as a Jena-friendly
	 * initial binding object.
	 * @return the initial binding
	 */
	QuerySolutionMap getInitialBinding();
	
	
	/**
	 * Gets this template call as a parsable SPARQL string, with all
	 * pre-bound argument variables inserted as constants.
	 * @return a SPARQL query string
	 * @deprecated  should not be used: has issues if sp:text is used only,
	 *              and may produce queries that in fact cannot be parsed back.
	 *              As an alternative, consider getting the Command and a
	 *              initial bindings mapping, then feed the QueryExecution with
	 *              that initial binding for execution.
	 */
	String getQueryString();
	
	
	/**
	 * Gets the associated Template, from the SPINModules registry.
	 * @return the template
	 */
	Template getTemplate();
}
