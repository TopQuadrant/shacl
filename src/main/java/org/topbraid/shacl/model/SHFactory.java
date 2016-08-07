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
import org.topbraid.shacl.arq.functions.TargetContainsPFunction;
import org.topbraid.shacl.model.impl.SHConstraintComponentImpl;
import org.topbraid.shacl.model.impl.SHFunctionImpl;
import org.topbraid.shacl.model.impl.SHParameterImpl;
import org.topbraid.shacl.model.impl.SHParameterizableImpl;
import org.topbraid.shacl.model.impl.SHParameterizableInstanceImpl;
import org.topbraid.shacl.model.impl.SHParameterizableTargetImpl;
import org.topbraid.shacl.model.impl.SHPropertyConstraintImpl;
import org.topbraid.shacl.model.impl.SHResultImpl;
import org.topbraid.shacl.model.impl.SHSPARQLConstraintImpl;
import org.topbraid.shacl.model.impl.SHSPARQLTargetImpl;
import org.topbraid.shacl.model.impl.SHShapeImpl;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.SimpleImplementation;

public class SHFactory {
    
    static {
		init(BuiltinPersonalities.model);
    }

    
	private static void init(Personality<RDFNode> p) {
		p.add(SHConstraintComponent.class, new SimpleImplementation(SH.ConstraintComponent.asNode(), SHConstraintComponentImpl.class));
		p.add(SHFunction.class, new SimpleImplementation(SH.Function.asNode(), SHFunctionImpl.class));
    	p.add(SHParameter.class, new SimpleImplementation(SH.Parameter.asNode(), SHParameterImpl.class));
    	p.add(SHParameterizable.class, new SimpleImplementation(SH.Parameterizable.asNode(), SHParameterizableImpl.class));
    	p.add(SHParameterizableInstance.class, new SimpleImplementation(RDFS.Resource.asNode(), SHParameterizableInstanceImpl.class));
    	p.add(SHParameterizableTarget.class, new SimpleImplementation(SH.Target.asNode(), SHParameterizableTargetImpl.class));
    	p.add(SHPropertyConstraint.class, new SimpleImplementation(SH.PropertyConstraint.asNode(), SHPropertyConstraintImpl.class));
    	p.add(SHResult.class, new SimpleImplementation(SH.AbstractResult.asNode(), SHResultImpl.class));
    	p.add(SHShape.class, new SimpleImplementation(SH.Shape.asNode(), SHShapeImpl.class));
		p.add(SHSPARQLConstraint.class, new SimpleImplementation(SH.SPARQLConstraint.asNode(), SHSPARQLConstraintImpl.class));
		p.add(SHSPARQLTarget.class, new SimpleImplementation(SH.SPARQLTarget.asNode(), SHSPARQLTargetImpl.class));
    	
		FunctionRegistry.get().put(SH.hasShape.getURI(), HasShapeFunction.class);
		PropertyFunctionRegistry.get().put(TargetContainsPFunction.URI, TargetContainsPFunction.class);
    }
	
	
	public static SHConstraintComponent asConstraintComponent(RDFNode resource) {
		return resource.as(SHConstraintComponent.class);
	}
	
	
	public static SHFunction asFunction(RDFNode resource) {
		return resource.as(SHFunction.class);
	}
	
	
	public static SHParameter asParameter(RDFNode resource) {
		return resource.as(SHParameter.class);
	}
	
	
	public static SHParameterizable asParameterizable(RDFNode resource) {
		return resource.as(SHParameterizable.class);
	}
	
	
	public static SHPropertyConstraint asPropertyConstraint(RDFNode node) {
		return node.as(SHPropertyConstraint.class);
	}
	
	
	public static SHShape asShape(RDFNode node) {
		return node.as(SHShape.class);
	}
	
	
	public static SHSPARQLConstraint asSPARQLConstraint(RDFNode node) {
		return node.as(SHSPARQLConstraint.class);
	}
	
	
	public static SHSPARQLTarget asSPARQLTarget(RDFNode node) {
		return node.as(SHSPARQLTarget.class);
	}
	
	
	public static SHParameterizableInstance asTemplateCall(RDFNode resource) {
		return resource.as(SHParameterizableInstance.class);
	}
	
	
	public static SHParameterizableConstraint asParameterizableConstraint(RDFNode node) {
		if(node instanceof Resource && isPropertyConstraint((Resource)node)) {
			return asPropertyConstraint(node);
		}
		else if(node instanceof Resource && isParameter((Resource)node)) {
			return asParameter(node);
		}
		else {
			return asShape(node);
		}
	}
	
	
	public static SHParameterizableTarget asParameterizableTarget(RDFNode node) {
		return node.as(SHParameterizableTarget.class);
	}

	
	public static boolean isSPARQLConstraint(RDFNode node) {
		return node instanceof Resource && 
				(JenaUtil.hasIndirectType((Resource)node, SH.SPARQLConstraint) ||
				(!((Resource)node).hasProperty(RDF.type) && node.getModel().contains(null, SH.sparql, node)));
	}
	
	
	public static boolean isSPARQLTarget(RDFNode node) {
		return node instanceof Resource && JenaUtil.hasIndirectType((Resource)node, SH.SPARQLTarget);
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
						node.getModel().contains(null, SH.parameter, node);
			}
			else if(r.hasProperty(RDF.type, SH.Shape) ||
					r.hasProperty(RDF.type, SH.PropertyConstraint) ||
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
				resource.getModel().contains(null, SH.property, resource);
	}
	
	
	public static boolean isPropertyConstraintWithPath(Resource resource) {
		return resource.hasProperty(SH.path) && isPropertyConstraint(resource);
	}
}
