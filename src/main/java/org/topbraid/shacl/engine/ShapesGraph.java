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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.model.SHConstraintComponent;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHShape;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Represents a shapes graph as input to an engine (e.g. validation or rule).
 * Basically it's a collection of Shapes.
 * 
 * @author Holger Knublauch
 */
public class ShapesGraph {
	
	private Predicate<Constraint> constraintFilter;
	
	private Map<Property,SHConstraintComponent> parametersMap = new HashMap<>();
	
	private List<Shape> rootShapes;
	
	private Predicate<SHShape> shapeFilter;
	
	private Map<Node,Shape> shapesMap = new HashMap<>();
	
	private Model shapesModel;

	
	/**
	 * Constructs a new ShapesGraph.
	 * @param shapesModel  the Model containing the shape definitions
	 */
	public ShapesGraph(Model shapesModel) {
		this.shapesModel = shapesModel;
	}

	
	public SHConstraintComponent getComponentWithParameter(Property parameter) {
		if(!parametersMap.containsKey(parameter)) {
			StmtIterator it = shapesModel.listStatements(null, SH.path, parameter);
			while(it.hasNext()) {
				Resource param = it.next().getSubject();
				if(!param.hasProperty(SH.optional, JenaDatatypes.TRUE)) {
					StmtIterator i2 = shapesModel.listStatements(null, SH.parameter, param);
					while(i2.hasNext()) {
						Resource r = i2.next().getSubject();
						if(JenaUtil.hasIndirectType(r, SH.ConstraintComponent)) {
							i2.close();
							it.close();
							SHConstraintComponent cc = SHFactory.asConstraintComponent(r);
							parametersMap.put(parameter, cc);
							return cc;
						}
					}
				}
			}
			parametersMap.put(parameter, null);
			return null;
		}
		else {
			return parametersMap.get(parameter);
		}
	}
	
	
	/**
	 * Gets all shapes that declare a target and pass the provided filter.
	 * @return the root shapes
	 */
	public List<Shape> getRootShapes() {
		if(rootShapes == null) {
			
			// Collect all shapes, as identified by target and/or type
			Set<Resource> candidates = new HashSet<Resource>();
			candidates.addAll(shapesModel.listSubjectsWithProperty(SH.target).toList());
			candidates.addAll(shapesModel.listSubjectsWithProperty(SH.targetClass).toList());
			candidates.addAll(shapesModel.listSubjectsWithProperty(SH.targetNode).toList());
			candidates.addAll(shapesModel.listSubjectsWithProperty(SH.targetObjectsOf).toList());
			candidates.addAll(shapesModel.listSubjectsWithProperty(SH.targetSubjectsOf).toList());
			for(Resource shape : JenaUtil.getAllInstances(shapesModel.getResource(SH.NodeShape.getURI()))) {
				if(JenaUtil.hasIndirectType(shape, RDFS.Class)) {
					candidates.add(shape);
				}
			}
			for(Resource shape : JenaUtil.getAllInstances(shapesModel.getResource(SH.PropertyShape.getURI()))) {
				if(JenaUtil.hasIndirectType(shape, RDFS.Class)) {
					candidates.add(shape);
				}
			}

			// Turn the shape Resource objects into Shape instances
			this.rootShapes = new LinkedList<Shape>();
			for(Resource candidate : candidates) {
				SHShape shape = SHFactory.asShape(candidate);
				if(shapeFilter == null || shapeFilter.test(shape)) {
					this.rootShapes.add(getShape(shape.asNode()));
				}
			}
		}
		return rootShapes;
	}
	
	
	public Shape getShape(Node node) {
		Shape shape = shapesMap.get(node);
		if(shape == null) {
			shape = new Shape(this, SHFactory.asShape(shapesModel.asRDFNode(node)));
			shapesMap.put(node, shape);
		}
		return shape;
	}
	
	
	public boolean isIgnoredConstraint(Constraint constraint) {
		return constraintFilter != null && !constraintFilter.test(constraint);
	}


	public boolean isIgnored(Node shapeNode) {
		if(shapeFilter == null) {
			return false;
		}
		SHShape shape = SHFactory.asShape(shapesModel.asRDFNode(shapeNode));
		return !shapeFilter.test(shape);
	}
	
	
	/**
	 * Sets a filter Predicate that can be used to ignore certain constraints.
	 * See for example CoreConstraintFilter.
	 * Such filters must return true if the Constraint should be used, false to ignore.
	 * This method should be called immediately after the constructor only.
	 * @param value  the new constraint filter
	 */
	public void setConstraintFilter(Predicate<Constraint> value) {
		this.constraintFilter = value;
	}
	
	
	/**
	 * Sets a filter Predicate that can be used to ignore certain shapes.
	 * Such filters must return true if the shape should be used, false to ignore.
	 * This method should be called immediately after the constructor only.
	 * @param value  the new shape filter
	 */
	public void setShapeFilter(Predicate<SHShape> value) {
		this.shapeFilter = value;
	}
}
