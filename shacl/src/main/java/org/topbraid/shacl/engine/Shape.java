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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.path.Path;
import org.topbraid.shacl.arq.SHACLPaths;
import org.topbraid.shacl.model.SHConstraintComponent;
import org.topbraid.shacl.model.SHParameter;
import org.topbraid.shacl.model.SHShape;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.jenax.util.RDFLabels;

/**
 * Represents a shape as input to an engine (e.g. validation or rule).
 * A Shape consists of a collection of Constraints.
 * 
 * @author Holger Knublauch
 */
public class Shape {
	
	private List<Constraint> constraints;
	
	private Path jenaPath;

	private SHShape shape;
	
	private ShapesGraph shapesGraph;
	
	
	public Shape(ShapesGraph shapesGraph, SHShape shape) {
		this.shape = shape;
		this.shapesGraph = shapesGraph;
		Resource path = shape.getPath();
		if(path != null && path.isAnon()) {
			jenaPath = (Path) SHACLPaths.getJenaPath(SHACLPaths.getPathString(path), path.getModel());
		}
	}
	
	
	public Iterable<Constraint> getConstraints() {
		if(constraints == null) {
			constraints = new LinkedList<>();
			Set<SHConstraintComponent> handled = new HashSet<>();
			for(Statement s : shape.listProperties().toList()) {
				SHConstraintComponent component = shapesGraph.getComponentWithParameter(s.getPredicate());
				if(component != null && !handled.contains(component)) {
					List<SHParameter> params = component.getParameters();
					if(params.size() == 1) {
						Constraint constraint = new Constraint(this, component, params, s.getObject());
						if(!shapesGraph.isIgnoredConstraint(constraint)) {
							constraints.add(constraint);
						}
					}
					else if(isComplete(params)) {
						handled.add(component);
						Constraint constraint = new Constraint(this, component, params, null);
						if(!shapesGraph.isIgnoredConstraint(constraint)) {
							constraints.add(constraint);
						}
					}
				}
			}
		}
		return constraints;
	}
	
	
	public Path getJenaPath() {
		return jenaPath;
	}
	
	
	public Double getOrder() {
		Statement s = shape.getProperty(SH.order);
		if(s != null && s.getObject().isLiteral()) {
			return s.getLiteral().getDouble();
		}
		else {
			return 0.0;
		}
	}
	
	
	public SHShape getShapeResource() {
		return shape;
	}
	
	
	public ShapesGraph getShapesGraph() {
		return shapesGraph;
	}
	
	
	private boolean isComplete(List<SHParameter> params) {
		for(SHParameter param : params) {
			if(!param.isOptional() && !shape.hasProperty(param.getPredicate())) {
				return false;
			}
		}
		return true;
	}
	
	
	@Override
    public String toString() {
		return RDFLabels.get().getLabel(getShapeResource());
	}
}
