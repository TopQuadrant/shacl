package org.topbraid.shacl.lite;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.topbraid.shacl.vocabulary.SHACL;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * A simple (non-SPARQL) implementation of the SHACL Core Profile based on Jena.
 * 
 * This is meant to be a demonstrator only at this stage.
 *  
 * @author Holger Knublauch
 */
public class SHACLLiteConstraintValidator {
	
	private Model queryModel;
	
	private Map<Resource, Set<Resource>> visitedNodes2Shapes = new HashMap<Resource, Set<Resource>>(); 
	
	
	/**
	 * Constructs a new validator for a given Model.
	 * @param queryModel  the query model to validate on
	 */
	public SHACLLiteConstraintValidator(Model queryModel) {
		this.queryModel = queryModel;
	}

	
	/**
	 * Take a Shape and a focus node and returns a Model of all SHACL constraint violations.
	 * @param shape  the Shape to validate against
	 * @param focusNode  the focus node
	 * @return a Model of constraint violations
	 */
	public Model validateNodeAgainstShape(Resource shape, Resource focusNode) {
		
		// Remember that we had seen this already
		Set<Resource> set = visitedNodes2Shapes.get(focusNode);
		if(set != null && set.contains(shape)) {
			throw new IllegalArgumentException("Unsupported recursion of shape " + shape + " for " + focusNode);
		}
		if(set == null) {
			set = new HashSet<Resource>();
			visitedNodes2Shapes.put(focusNode, set);
		}
		set.add(shape);
		
		Model resultModel = ModelFactory.createDefaultModel();
		Set<Resource> shapes = new HashSet<Resource>();
		addSuperClasses(shape, shapes);
		for(Resource s : shapes) {
			for(Statement propertyS : s.listProperties(SHACL.property).toList()) {
				resultModel.add(validateConstraint(propertyS.getResource(), focusNode));
			}
			for(Statement constraintS : s.listProperties(SHACL.constraint).toList()) {
				resultModel.add(validateConstraint(constraintS.getResource(), focusNode));
			}
		}
		return resultModel;
	}
	
	
	/**
	 * Performs validation of all resources in the current query model.
	 * @return a Model of constraint violations
	 */
	public Model validateGraph() {
		Model resultModel = ModelFactory.createDefaultModel();
		for(Statement s : queryModel.listStatements(null, RDF.type, (RDFNode)null).toList()) {
			resultModel.add(new SHACLLiteConstraintValidator(queryModel).validateNodeAgainstShape(s.getResource(), s.getSubject()));
		}
		for(Statement s : queryModel.listStatements(null, SHACL.nodeShape, (RDFNode)null).toList()) {
			resultModel.add(new SHACLLiteConstraintValidator(queryModel).validateNodeAgainstShape(s.getResource(), s.getSubject()));
		}
		return resultModel;
	}

	
	/**
	 * Takes a single constraint and a focus node and returns a Model of all SHACL constraint violations.
	 * @param constraint  the Resource representing the constraint
	 * @param focusNode  the focus node
	 * @return a Model of constraint violations
	 */
	public Model validateConstraint(Resource constraint, Resource focusNode) {
		
		Model resultModel = ModelFactory.createDefaultModel();
		
		if(constraint.hasProperty(RDF.type, SHACL.OrConstraint)) {
			RDFList list = constraint.getProperty(SHACL.shapes).getResource().as(RDFList.class);
			boolean valid = false;
			for(RDFNode member : list.iterator().toList()) {
				if(hasShape(focusNode, member.asResource())) {
					valid = true;
					break;
				}
			}
			if(!valid) {
				Resource error = resultModel.createResource(SHACL.Error);
				error.addProperty(SHACL.root, focusNode);
				error.addProperty(SHACL.message, "Resource " + focusNode + " does not match OrConstraint");
			}
			return resultModel;
		}
		
		// Property constraints
		Statement predicateS = constraint.getProperty(SHACL.predicate);
		if(predicateS == null || !predicateS.getObject().isURIResource()) {
			return resultModel; // Or throw exception
		}
		
		Property predicate = queryModel.getProperty(predicateS.getResource().getURI());
		
		// sh:allowedValues
		Statement allowedValuesS = constraint.getProperty(SHACL.allowedValues);
		if(allowedValuesS != null && allowedValuesS.getObject().isResource()) {
			validateAllowedValues(focusNode, predicate, allowedValuesS.getResource(), resultModel);
		}
		
		// sh:hasValue
		Statement hasValueS = constraint.getProperty(SHACL.hasValue);
		if(hasValueS != null) {
			validateHasValueConstraint(focusNode, predicate, hasValueS.getObject(), resultModel);
		}
		
		// sh:minCount and sh:maxCount
		Statement minCountS = constraint.getProperty(SHACL.minCount);
		Statement maxCountS = constraint.getProperty(SHACL.maxCount);
		if(minCountS != null || maxCountS != null) {
			validateCountConstraint(
					focusNode,
					predicate,
					minCountS != null ? minCountS.getObject() : null, 
					maxCountS != null ? maxCountS.getObject() : null, 
					resultModel);
		}
		
		// sh:nodeType
		Statement nodeTypeS = constraint.getProperty(SHACL.nodeKind);
		if(nodeTypeS != null) {
			validateNodeTypeConstraint(focusNode, predicate, nodeTypeS.getObject(), resultModel);
		}
		
		// sh:valueShape
		Statement shapeS = constraint.getProperty(SHACL.valueShape);
		if(shapeS != null && shapeS.getObject().isResource()) {
			validateShapeConstraint(focusNode, predicate, shapeS.getResource(), resultModel);
		}
		
		return resultModel;
	}
	
	
	private void addSuperClasses(Resource shape, Set<Resource> shapes) {
		shapes.add(shape);
		for(Statement s : shape.listProperties(RDFS.subClassOf).toList()) {
			if(!shapes.contains(s.getResource())) {
				addSuperClasses(s.getResource(), shapes);
			}
		}
	}
	
	
	// Equivalent of sh:hasNodeType function
	private boolean hasNodeType(RDFNode node, RDFNode nodeType) {
		if(SHACL.IRI.equals(nodeType) && node.isURIResource()) {
			return true;
		}
		else if(SHACL.Literal.equals(nodeType) && node.isLiteral()) {
			return true;
		}
		else if(SHACL.BlankNode.equals(nodeType) && node.isAnon()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	private boolean hasShape(Resource focusNode, Resource shape) {
		return validateNodeAgainstShape(shape, focusNode).size() == 0;
	}
	
	
	// Validates sh:allowedValues
	private void validateAllowedValues(Resource focusNode, Property predicate, Resource allowedValues, Model resultModel) {
		for(Statement s : focusNode.listProperties(predicate).toList()) {
			if(!allowedValues.hasProperty(SHACL.member, s.getObject())) {
				Resource error = resultModel.createResource(SHACL.Error);
				error.addProperty(SHACL.root, focusNode);
				error.addProperty(SHACL.subject, focusNode);
				error.addProperty(SHACL.predicate, predicate);
				error.addProperty(SHACL.object, s.getObject());
				error.addProperty(SHACL.message, "Value " + s.getObject() + " not among the allowed values set " + allowedValues);
			}
		}		
	}
	

	// Validates sh:minCount and sh:maxCount
	private void validateCountConstraint(Resource focusNode, Property predicate, RDFNode minCountNode, RDFNode maxCountNode, Model resultModel) {
		int count = focusNode.listProperties(predicate).toList().size();
		if((minCountNode != null && count < minCountNode.asLiteral().getInt()) ||
			(maxCountNode != null && count > maxCountNode.asLiteral().getInt())) {
			Resource error = resultModel.createResource(SHACL.Error);
			error.addProperty(SHACL.root, focusNode);
			error.addProperty(SHACL.subject, focusNode);
			error.addProperty(SHACL.predicate, predicate);
			error.addProperty(SHACL.message, "Cardinality of " + count + " does not match [" + 
					(minCountNode != null ? minCountNode.asLiteral().getLexicalForm() : "") + ".." +
					(maxCountNode != null ? maxCountNode.asLiteral().getLexicalForm() : "") + "]");
		}
	}

	
	// Validates sh:hasValue
	private void validateHasValueConstraint(Resource focusNode, Property predicate, RDFNode hasValueNode, Model resultModel) {
		if(!focusNode.hasProperty(predicate, hasValueNode)) {
			Resource error = resultModel.createResource(SHACL.Error);
			error.addProperty(SHACL.root, focusNode);
			error.addProperty(SHACL.subject, focusNode);
			error.addProperty(SHACL.predicate, predicate);
			error.addProperty(SHACL.message, "Missing required value " + hasValueNode);
		}
	}

	
	// Validates sh:nodeType
	private void validateNodeTypeConstraint(Resource focusNode, Property predicate, RDFNode nodeType, Model resultModel) {
		for(Statement s : focusNode.listProperties(predicate).toList()) {
			if(!hasNodeType(s.getObject(), nodeType)) {
				Resource error = resultModel.createResource(SHACL.Error);
				error.addProperty(SHACL.root, focusNode);
				error.addProperty(SHACL.subject, focusNode);
				error.addProperty(SHACL.predicate, predicate);
				error.addProperty(SHACL.object, s.getObject());
				error.addProperty(SHACL.message, "Value " + s.getObject() + " does not have node type " + nodeType.asResource().getLocalName());
			}
		}
	}

	
	// Validates sh:valueShape
	private void validateShapeConstraint(Resource focusNode, Property predicate, Resource valueShape, Model resultModel) {
		for(Statement s : focusNode.listProperties(predicate).toList()) {
			if(!hasShape(s.getResource(), valueShape)) {
				Resource error = resultModel.createResource(SHACL.Error);
				error.addProperty(SHACL.root, focusNode);
				error.addProperty(SHACL.subject, focusNode);
				error.addProperty(SHACL.predicate, predicate);
				error.addProperty(SHACL.object, s.getObject());
				error.addProperty(SHACL.message, "Value " + s.getObject() + " does not have shape " + valueShape.asResource());
			}
		}
	}
}
