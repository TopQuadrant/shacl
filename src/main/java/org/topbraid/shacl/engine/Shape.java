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
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.path.P_Inverse;
import org.apache.jena.sparql.path.P_Link;
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
 * A Shape is mainly a collection of Constraints.
 * 
 * @author Holger Knublauch
 */
public class Shape {
	
	// The Constraints of this shape
	private List<Constraint> constraints;
	
	// True if this is sh:deactivated
	private boolean deactivated;

	// The Jena Path if this is a property shape with a path expression, see also predicate
	private Path jenaPath;
	
	// The sh:path
	private Resource path;

	// The predicate if this is a property shape with a IRI sh:path
	private Property predicate;

	// The values of sh:message, computed once on demand
	private List<RDFNode> messages;
	
	// True if this is a node shape, i.e. no path
	private boolean nodeShape;

	// The sh:severity
	private Resource severity;

	// The resource of the shape in the shapes graph's Jena Model to query details when needed
	private SHShape shape;
	
	// The owning ShapesGraph
	private ShapesGraph shapesGraph;
	
	// The Targets that this shape declares (e.g., based on sh:targetClass)
	private List<Target> targets = new ArrayList<>();
	
	
	/**
	 * Constructs a new Shape in a given ShapesGraph.
	 * @param shapesGraph  the owning ShapesGraph
	 * @param shape  the Jena resource of the shape
	 */
	public Shape(ShapesGraph shapesGraph, SHShape shape) {
		this.shape = shape;
		this.shapesGraph = shapesGraph;
		this.deactivated = shape.isDeactivated();
		this.severity = shape.getSeverity();
		Resource path = shape.getPath();
		if(path != null) {			
			this.path = path;
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
	
	
	public synchronized Iterable<Constraint> getConstraints() {
		if(constraints == null) {
			constraints = new LinkedList<>();
			Set<SHConstraintComponent> handled = new HashSet<>();
			for(Statement s : shape.listProperties().toList()) {
				SHConstraintComponent component = shapesGraph.getComponentWithParameter(s.getPredicate());
				if(component != null && !handled.contains(component)) {
					List<SHParameter> params = component.getParameters();
					if(params.size() == 1) {
						Constraint constraint = shapesGraph.createConstraint(this, component, params, s.getObject());
						if(!shapesGraph.isIgnoredConstraint(constraint)) {
							constraints.add(constraint);
						}
					}
					else if(isComplete(params)) {
						handled.add(component);
						Constraint constraint = shapesGraph.createConstraint(this, component, params, null);
						if(!shapesGraph.isIgnoredConstraint(constraint)) {
							constraints.add(constraint);
						}
					}
				}
			}
		}
		return constraints;
	}
	
	
	public Property getInversePredicate() {
		if(jenaPath instanceof P_Inverse && ((P_Inverse)jenaPath).getSubPath() instanceof P_Link) {
			return ResourceFactory.createProperty(((P_Link)((P_Inverse)jenaPath).getSubPath()).getNode().getURI());
		}
		return null;
	}
	
	
	public Path getJenaPath() {
		return jenaPath;
	}
	
	
	public synchronized Collection<RDFNode> getMessages() {
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
