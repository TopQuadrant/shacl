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
package org.topbraid.shacl.validation.js;

import java.util.List;

import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.model.SHJSConstraint;
import org.topbraid.shacl.model.SHJSExecutable;
import org.topbraid.shacl.validation.ConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Executes sh:js constraints.
 * 
 * @author Holger Knublauch
 */
public class JSConstraintExecutor extends AbstractJSExecutor implements ConstraintExecutor {

	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine validationEngine, List<RDFNode> focusNodes) {
		
		SHJSConstraint js = constraint.getParameterValue().as(SHJSConstraint.class);
		
		if(js.isDeactivated()) {
			return;
		}

		super.executeConstraint(constraint, validationEngine, focusNodes);
	}


	@Override
	protected void addBindings(Constraint constraint, QuerySolutionMap bindings) {
		// Do nothing
	}

	
	@Override
	protected SHJSExecutable getExecutable(Constraint constraint) {
		return constraint.getParameterValue().as(SHJSConstraint.class);
	}


	@Override
	protected String getLabel(Constraint constraint) {
		return "JavaScript Constraint " + JenaUtil.getStringProperty((Resource)constraint.getParameterValue(), SH.jsFunctionName);
	}


	@Override
	protected List<RDFNode> getValueNodes(ValidationEngine validationEngine, Constraint constraint, QuerySolutionMap bindings, RDFNode focusNode) {
		return validationEngine.getValueNodes(constraint, focusNode);
	}
}
