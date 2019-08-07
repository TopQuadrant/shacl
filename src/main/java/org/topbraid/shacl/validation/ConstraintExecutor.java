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
 * Implementation of this include those using SPARQL or JavaScript constraint components
 * but also special handlers for sh:property, sh:sparql and sh:js.
 * 
 * @author Holger Knublauch
 */
public interface ConstraintExecutor {

	void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes);
}
