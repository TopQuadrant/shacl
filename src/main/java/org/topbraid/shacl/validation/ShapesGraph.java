package org.topbraid.shacl.validation;

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
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.model.SHConstraintComponent;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHParameter;
import org.topbraid.shacl.model.SHShape;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

/**
 * Represents a shapes graph during validation.
 * Basically it's a collection of Shapes.
 * 
 * @author Holger Knublauch
 */
public class ShapesGraph {
	
	private Predicate<SHShape> filter;
	
	private Map<Property,SHConstraintComponent> parametersMap;
	
	private List<Shape> rootShapes;
	
	private Map<Node,Shape> shapesMap = new HashMap<>();
	
	private Model shapesModel;
	
	
	/**
	 * Constructs a new VShapesGraph.
	 * @param shapesModel  the Model containing the shape definitions
	 * @param filter  a predicate to filter all shapes by or null to allow all
	 */
	public ShapesGraph(Model shapesModel, Predicate<SHShape> filter) {
		this.filter = filter;
		this.shapesModel = shapesModel;
	}
	
	
	private void computeParametersMap() {
		if(parametersMap == null) {
			parametersMap = new HashMap<>();
			for(Resource cc : JenaUtil.getAllInstances(SH.ConstraintComponent.inModel(shapesModel))) {
				SHConstraintComponent component = SHFactory.asConstraintComponent(cc);
				for(SHParameter param : component.getParameters()) {
					if(!param.isOptional()) {
						parametersMap.put(param.getPredicate(), component);
					}
				}
			}
		}
	}
	
	
	public SHConstraintComponent getComponentWithParameter(Property parameter) {
		computeParametersMap();
		return parametersMap.get(parameter);
	}
	
	
	/**
	 * Gets all shapes that declare a target and pass the provided filter.
	 * @param rootFilter  a shapes filter or null to allow all shapes.
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

			// Turn the shape Resource objects into VShapes
			this.rootShapes = new LinkedList<Shape>();
			for(Resource candidate : candidates) {
				SHShape shape = SHFactory.asShape(candidate);
				if(filter == null || filter.test(shape)) {
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


	public boolean isIgnored(Node shapeNode) {
		if(filter == null) {
			return false;
		}
		SHShape shape = SHFactory.asShape(shapesModel.asRDFNode(shapeNode));
		return !filter.test(shape);
	}
}
