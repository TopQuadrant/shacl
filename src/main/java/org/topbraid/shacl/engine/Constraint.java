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
package org.topbraid.shacl.engine;

import java.util.Collection;
import java.util.List;

import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.model.SHConstraintComponent;
import org.topbraid.shacl.model.SHParameter;
import org.topbraid.shacl.model.SHShape;
import org.topbraid.shacl.validation.ConstraintExecutor;
import org.topbraid.shacl.validation.ConstraintExecutors;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Represents a constraint as input to an engine (e.g. validation or inferencing).
 * Here, a constraint is the combination of parameters, e.g. a specific value
 * of sh:datatype at a given Shape.
 * 
 * @author Holger Knublauch
 */
public class Constraint {
	
	// The constraint component resource, e.g. sh:DatatypeConstraintComponent
	private SHConstraintComponent component;
	
	// The constraint executor, computed on demand
	private ConstraintExecutor executor;
	
	// The parameter value that triggered this, e.g. the specific sh:datatype triple object
	private RDFNode parameterValue;

	// The list of parameters
	private List<SHParameter> params;

	// The owning Shape
	private Shape shape;
	
	
	// Should only be instantiated from ShapesGraph.createConstraint
	public Constraint(Shape shape, SHConstraintComponent component, List<SHParameter> params, RDFNode parameterValue) {
		this.component = component;
		this.params = params;
		this.parameterValue = parameterValue;
		this.shape = shape;
	}

	
	public void addBindings(QuerySolutionMap map) {
		if(parameterValue != null) {
			SHParameter param = params.get(0);
			if(!map.contains(param.getVarName())) {
				map.add(param.getVarName(), parameterValue);
			}
		}
		else {
			for(SHParameter param : params) {
				String varName = param.getVarName();
				if(!map.contains(varName)) {
					RDFNode parameterValue = JenaUtil.getProperty(shape.getShapeResource(), param.getPredicate());
					if(parameterValue != null) {
						map.add(varName, parameterValue);
					}
				}
			}
		}
	}
	
	
	public SHConstraintComponent getComponent() {
		return component;
	}
	
	
	public Resource getContext() {
		if(shape.getShapeResource().hasProperty(SH.path)) {
			return SH.PropertyShape;
		}
		else {
			return SH.NodeShape;
		}
	}
	
	
	public synchronized ConstraintExecutor getExecutor() {
		if(executor == null) {
			executor = ConstraintExecutors.get().getExecutor(this);
		}
		return executor;
	}

	
	public Collection<RDFNode> getMessages() {
		return shape.getMessages();
	}
	
	
	public List<SHParameter> getParameters() {
		return params;
	}

	
	public RDFNode getParameterValue() {
		return parameterValue;
	}
	
	
	public Resource getSeverity() {
		return shape.getSeverity();
	}
	
	
	public Shape getShape() {
		return shape;
	}
	
	
	public SHShape getShapeResource() {
		return shape.getShapeResource();
	}
	
	
	@Override
    public String toString() {
		return "Constraint " + component.getLocalName() + " at " + shape;
	}
}
