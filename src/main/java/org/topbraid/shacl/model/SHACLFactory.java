package org.topbraid.shacl.model;

import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.arq.functions.HasShapeFunction;
import org.topbraid.shacl.arq.functions.ScopeContainsPFunction;
import org.topbraid.shacl.model.impl.SHACLConstraintComponentImpl;
import org.topbraid.shacl.model.impl.SHACLFunctionImpl;
import org.topbraid.shacl.model.impl.SHACLInversePropertyConstraintImpl;
import org.topbraid.shacl.model.impl.SHACLParameterImpl;
import org.topbraid.shacl.model.impl.SHACLParameterizableConstraintImpl;
import org.topbraid.shacl.model.impl.SHACLParameterizableImpl;
import org.topbraid.shacl.model.impl.SHACLParameterizableInstanceImpl;
import org.topbraid.shacl.model.impl.SHACLParameterizableScopeImpl;
import org.topbraid.shacl.model.impl.SHACLPropertyConstraintImpl;
import org.topbraid.shacl.model.impl.SHACLResultImpl;
import org.topbraid.shacl.model.impl.SHACLSPARQLConstraintImpl;
import org.topbraid.shacl.model.impl.SHACLSPARQLScopeImpl;
import org.topbraid.shacl.model.impl.SHACLShapeImpl;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.SimpleImplementation;

public class SHACLFactory {
    
    static {
		init(BuiltinPersonalities.model);
    }

    
	private static void init(Personality<RDFNode> p) {
		p.add(SHACLConstraintComponent.class, new SimpleImplementation(SH.ConstraintComponent.asNode(), SHACLConstraintComponentImpl.class));
		p.add(SHACLFunction.class, new SimpleImplementation(SH.Function.asNode(), SHACLFunctionImpl.class));
    	p.add(SHACLInversePropertyConstraint.class, new SimpleImplementation(SH.InversePropertyConstraint.asNode(), SHACLInversePropertyConstraintImpl.class));
    	p.add(SHACLParameter.class, new SimpleImplementation(SH.Parameter.asNode(), SHACLParameterImpl.class));
    	p.add(SHACLParameterizable.class, new SimpleImplementation(SH.Parameterizable.asNode(), SHACLParameterizableImpl.class));
    	p.add(SHACLParameterizableInstance.class, new SimpleImplementation(RDFS.Resource.asNode(), SHACLParameterizableInstanceImpl.class));
    	p.add(SHACLParameterizableConstraint.class, new SimpleImplementation(SH.Constraint.asNode(), SHACLParameterizableConstraintImpl.class));
    	p.add(SHACLParameterizableScope.class, new SimpleImplementation(SH.Scope.asNode(), SHACLParameterizableScopeImpl.class));
    	p.add(SHACLPropertyConstraint.class, new SimpleImplementation(SH.PropertyConstraint.asNode(), SHACLPropertyConstraintImpl.class));
    	p.add(SHACLResult.class, new SimpleImplementation(SH.AbstractResult.asNode(), SHACLResultImpl.class));
    	p.add(SHACLShape.class, new SimpleImplementation(SH.Shape.asNode(), SHACLShapeImpl.class));
		p.add(SHACLSPARQLConstraint.class, new SimpleImplementation(SH.SPARQLConstraint.asNode(), SHACLSPARQLConstraintImpl.class));
		p.add(SHACLSPARQLScope.class, new SimpleImplementation(SH.SPARQLScope.asNode(), SHACLSPARQLScopeImpl.class));
    	
		FunctionRegistry.get().put(SH.hasShape.getURI(), HasShapeFunction.class);
		PropertyFunctionRegistry.get().put(ScopeContainsPFunction.URI, ScopeContainsPFunction.class);
    }
	
	
	public static SHACLConstraintComponent asConstraintComponent(RDFNode resource) {
		return resource.as(SHACLConstraintComponent.class);
	}
	
	
	public static SHACLFunction asFunction(RDFNode resource) {
		return resource.as(SHACLFunction.class);
	}
	
	
	public static SHACLInversePropertyConstraint asInversePropertyConstraint(RDFNode node) {
		return node.as(SHACLInversePropertyConstraint.class);
	}
	
	
	public static SHACLParameter asParameter(RDFNode resource) {
		return resource.as(SHACLParameter.class);
	}
	
	
	public static SHACLParameterizable asParameterizable(RDFNode resource) {
		return resource.as(SHACLParameterizable.class);
	}
	
	
	public static SHACLPropertyConstraint asPropertyConstraint(RDFNode node) {
		return node.as(SHACLPropertyConstraint.class);
	}
	
	
	public static SHACLShape asShape(RDFNode node) {
		return node.as(SHACLShape.class);
	}
	
	
	public static SHACLSPARQLConstraint asSPARQLConstraint(RDFNode node) {
		return node.as(SHACLSPARQLConstraint.class);
	}
	
	
	public static SHACLSPARQLScope asSPARQLScope(RDFNode node) {
		return node.as(SHACLSPARQLScope.class);
	}
	
	
	public static SHACLParameterizableInstance asTemplateCall(RDFNode resource) {
		return resource.as(SHACLParameterizableInstance.class);
	}
	
	
	public static SHACLParameterizableConstraint asParameterizableConstraint(RDFNode node) {
		return node.as(SHACLParameterizableConstraint.class);
	}
	
	
	public static SHACLParameterizableScope asParameterizableScope(RDFNode node) {
		return node.as(SHACLParameterizableScope.class);
	}
	
	
	public static boolean isInversePropertyConstraint(Resource resource) {
		return resource.hasProperty(RDF.type, SH.InversePropertyConstraint) ||
				(!resource.hasProperty(RDF.type) && resource.getModel().contains(null, SH.inverseProperty, resource));
	}
	
	
	public static boolean isNodeConstraint(Resource resource) {
		return resource.hasProperty(RDF.type, SH.NodeConstraint) ||
				(!resource.hasProperty(RDF.type) && resource.getModel().contains(null, SH.constraint, resource));
	}

	
	public static boolean isSPARQLConstraint(RDFNode node) {
		return node instanceof Resource && JenaUtil.hasIndirectType((Resource)node, SH.SPARQLConstraint);
	}
	
	
	public static boolean isSPARQLScope(RDFNode node) {
		return node instanceof Resource && JenaUtil.hasIndirectType((Resource)node, SH.SPARQLScope);
	}
	
	
	/**
	 * Checks if a given node is a Shape.  Note this is just an approximation based
	 * on a couple of hard-coded properties.  It should really rely on sh:defaultValueType.
	 * @param node  the node to test
	 * @return true if node is a Shape
	 */
	public static boolean isShape(RDFNode node) {
		// TODO: Make it rely on sh:defaultValueType
		if(node instanceof Resource) {
			if(JenaUtil.hasIndirectType((Resource)node, SH.Shape)) {
				return true;
			}
			else if(node.isAnon() && !((Resource)node).hasProperty(RDF.type)) {
				if(node.getModel().contains(null, SH.shape, node) ||
						node.getModel().contains(null, SH.filterShape, node)) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	public static boolean isParameterizableConstraint(RDFNode node) {
		if(node instanceof Resource) {
			Resource r = (Resource) node;
			if(!r.hasProperty(RDF.type)) {
				return  node.getModel().contains(null, SH.property, node) ||
						node.getModel().contains(null, SH.parameter, node) ||
						node.getModel().contains(null, SH.inverseProperty, node) ||
						node.getModel().contains(null, SH.constraint, node);
			}
			else if(r.hasProperty(RDF.type, SH.NodeConstraint) ||
					r.hasProperty(RDF.type, SH.PropertyConstraint) ||
					r.hasProperty(RDF.type, SH.InversePropertyConstraint) ||
					r.hasProperty(RDF.type, SH.Parameter)) {
				return true;
			}
		}
		return false;
	}
	
	
	public static boolean isParameter(Resource resource) {
		return resource.hasProperty(RDF.type, SH.Parameter) ||
				(!resource.hasProperty(RDF.type) && resource.getModel().contains(null, SH.parameter, resource));
	}
    
    
	/**
	 * Checks if a given RDFNode represents a parameterizable instance.
	 * It either needs to be an instance of an instance of sh:Parameterizable,
	 * or be a typeless node that has an incoming edge via a property 
	 * that has a declared sh:defaultType, such as sh:property.
	 * @param node  the node to check
	 * @return true if node is a parameterizable instance
	 */
	public static boolean isParameterizableInstance(RDFNode node) {
		if(node instanceof Resource) {
			Resource resource = (Resource) node;
			
			// Return true if this has sh:Parameterizable as its metaclass
			for(Resource type : JenaUtil.getTypes(resource)) {
				if(JenaUtil.hasIndirectType(type, SH.Parameterizable)) {
					return true;
				}
			}
			
			// If this is a typeless node, check for defaultType of incoming references
			if(!resource.hasProperty(RDF.type)) {
				Resource dt = SHACLUtil.getResourceDefaultType(resource);
				if(dt != null && JenaUtil.hasIndirectType(dt, SH.Parameterizable)) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	public static boolean isPropertyConstraint(Resource resource) {
		return resource.hasProperty(RDF.type, SH.PropertyConstraint) ||
				(!resource.hasProperty(RDF.type) && resource.getModel().contains(null, SH.property, resource));
	}
}
