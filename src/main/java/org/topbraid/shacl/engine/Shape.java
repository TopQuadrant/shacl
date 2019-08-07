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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.jenax.util.RDFLabels;
import org.topbraid.shacl.arq.SHACLPaths;
import org.topbraid.shacl.model.SHConstraintComponent;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHParameter;
import org.topbraid.shacl.model.SHParameterizableTarget;
import org.topbraid.shacl.model.SHShape;
import org.topbraid.shacl.targets.CustomTargetLanguage;
import org.topbraid.shacl.targets.CustomTargets;
import org.topbraid.shacl.targets.InstancesTarget;
import org.topbraid.shacl.targets.NodeTarget;
import org.topbraid.shacl.targets.ObjectsOfTarget;
import org.topbraid.shacl.targets.SubjectsOfTarget;
import org.topbraid.shacl.targets.Target;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Represents a shape as input to an engine (e.g. validation or rule).
 * A Shape consists of a collection of Constraints.
 * 
 * @author Holger Knublauch
 */
public class Shape {
	
	private List<Constraint> constraints;
	
	private boolean deactivated;
	
	private Path jenaPath;
	
	private Resource path;
	
	private Property predicate;
	
	private List<RDFNode> messages;
	
	private boolean nodeShape;
	
	private Resource severity;

	private SHShape shape;
	
	private ShapesGraph shapesGraph;
	
	private List<Target> targets = new ArrayList<>();
	
	
	public Shape(ShapesGraph shapesGraph, SHShape shape) {
		this.shape = shape;
		this.shapesGraph = shapesGraph;
		Resource path = shape.getPath();
		this.path = path;
		this.deactivated = shape.isDeactivated();
		this.severity = shape.getSeverity();
		if(path != null) {			
			if(path.isAnon()) {
				jenaPath = (Path) SHACLPaths.getJenaPath(SHACLPaths.getPathString(path), path.getModel());
			}
			else {
				predicate = JenaUtil.asProperty(path);
			}
		}
		else {
			nodeShape = true;
		}
		collectTargets();
	}

	
	private void collectTargets() {
		
		if(JenaUtil.hasIndirectType(shape, RDFS.Class)) {
			targets.add(new InstancesTarget(shape));
		}
		
		for(Resource targetClass : JenaUtil.getResourceProperties(shape, SH.targetClass)) {
			targets.add(new InstancesTarget(targetClass));
		}
		
		for(RDFNode targetNode : shape.getModel().listObjectsOfProperty(shape, SH.targetNode).toList()) {
			targets.add(new NodeTarget(targetNode));
		}
		
		for(Resource sof : JenaUtil.getResourceProperties(shape, SH.targetSubjectsOf)) {
			targets.add(new SubjectsOfTarget(JenaUtil.asProperty(sof)));
		}
		
		for(Resource sof : JenaUtil.getResourceProperties(shape, SH.targetObjectsOf)) {
			targets.add(new ObjectsOfTarget(JenaUtil.asProperty(sof)));
		}
		
		for(Resource target : JenaUtil.getResourceProperties(shape, SH.target)) {
			Resource executable = target;
			SHParameterizableTarget parameterizableTarget = null;
			if(SHFactory.isParameterizableInstance(target)) {
				parameterizableTarget = SHFactory.asParameterizableTarget(target);
				executable = parameterizableTarget.getParameterizable();
			}
			CustomTargetLanguage language = CustomTargets.get().getLanguageForTarget(executable);
			Target t = language.createTarget(executable, parameterizableTarget);
			targets.add(t);
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
	
	
	public Collection<RDFNode> getMessages() {
		if(messages == null) {
			messages = shape.listProperties(SH.message).mapWith(s -> s.getObject()).toList();
		}
		return messages;
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
	
	
	public Resource getPath() {
		return path;
	}
	
	
	public Property getPredicate() {
		return predicate;
	}
	
	
	public Resource getSeverity() {
		return severity;
	}
	
	
	public SHShape getShapeResource() {
		return shape;
	}
	
	
	public ShapesGraph getShapesGraph() {
		return shapesGraph;
	}

	
    public Set<RDFNode> getTargetNodes(Dataset dataset) {
        Set<RDFNode> results = new HashSet<>();
        for(Target target : targets) {
        	target.addTargetNodes(dataset, results);
        }
        return results;
    }
    
    
    public List<Target> getTargets() {
    	return targets;
    }
	
	
	private boolean isComplete(List<SHParameter> params) {
		for(SHParameter param : params) {
			if(!param.isOptional() && !shape.hasProperty(param.getPredicate())) {
				return false;
			}
		}
		return true;
	}
	
	
	public boolean isDeactivated() {
		return deactivated;
	}
	
	
	public boolean isNodeShape() {
		return nodeShape;
	}
	
	
	@Override
    public String toString() {
		return RDFLabels.get().getLabel(getShapeResource());
	}
}
