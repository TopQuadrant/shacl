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

import org.topbraid.spin.model.print.Printable;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;


/**
 * The base interface of TriplePattern and TripleTemplate.
 * 
 * @author Holger Knublauch
 */
public interface Triple extends Printable, Resource {
	
	/**
	 * Gets the subject of this Triple, downcasting it into Variable if appropriate.
	 * @return the subject
	 */
	RDFNode getSubject();
	

	/**
	 * Gets the predicate of this Triple, downcasting it into Variable if appropriate.
	 * @return the predicate
	 */
	Resource getPredicate();
	
	
	/**
	 * Gets the object of this Triple, downcasting it into Variable if appropriate.
	 * @return the object
	 */
	RDFNode getObject();
	
	
	/**
	 * Gets the object as a Resource.
	 * @return the object or null if it's not a resource (i.e., a literal)
	 */
	Resource getObjectResource();
}
