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
package org.topbraid.shacl.validation;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.shacl.engine.Constraint;

/**
 * Interface for objects that can execute a given constraint.
 * 
 * Implementations of this include those using SPARQL or JavaScript constraint components
 * but also natively implemented handlers for sh:property, sh:class and the other SHACL core constraint types.
 * 
 * @author Holger Knublauch
 */
public interface ConstraintExecutor {

	/**
	 * Validates a collection of focus nodes against a constraint.
	 * Implementations are typically calling engine.createValidationResult() to record results such as violations.
	 * @param constraint  the Constraint (instance) to validate (e.g. a specific sh:datatype constraint)
	 * @param engine  the ValidationEngine
	 * @param focusNodes  the collection of focus nodes - should not contain duplicates
	 */
	void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes);
}
