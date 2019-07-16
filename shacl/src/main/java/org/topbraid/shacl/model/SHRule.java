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
package org.topbraid.shacl.model;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public interface SHRule extends SHResource {
	
	/**
	 * Gets the sh:subject (assuming this is a triple rule)
	 * @return the subject of the triple rule
	 */
	RDFNode getSubject();
	
	/**
	 * Gets the sh:subject (assuming this is a triple rule)
	 * @return the subject of the triple rule
	 */
	Resource getPredicate();
	
	/**
	 * Gets the sh:subject (assuming this is a triple rule)
	 * @return the subject of the triple rule
	 */
	RDFNode getObject();
	
	
	/**
	 * Checks if this rule is an instance of sh:JSRule
	 * @return true if this is a sh:JSRule
	 */
	boolean isJSRule();

	
	/**
	 * Checks if this rule is an instance of sh:SPARQLRule
	 * @return true if this is a sh:SPARQLRule
	 */
	boolean isSPARQLRule();

	
	/**
	 * Checks if this rule is an instance of sh:TripleRule
	 * @return true if this is a sh:TripleRule
	 */
	boolean isTripleRule();
}
