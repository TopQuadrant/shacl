package org.topbraid.shacl.model;

import org.topbraid.shacl.arq.functions.HasShapeFunction;
import org.topbraid.shacl.arq.functions.ScopeContainsPFunction;
import org.topbraid.shacl.model.impl.SHACLArgumentImpl;
import org.topbraid.shacl.model.impl.SHACLFunctionImpl;
import org.topbraid.shacl.model.impl.SHACLNativeConstraintImpl;
import org.topbraid.shacl.model.impl.SHACLNativeScopeImpl;
import org.topbraid.shacl.model.impl.SHACLPropertyConstraintImpl;
import org.topbraid.shacl.model.impl.SHACLResultImpl;
import org.topbraid.shacl.model.impl.SHACLShapeImpl;
import org.topbraid.shacl.model.impl.SHACLTemplateCallImpl;
import org.topbraid.shacl.model.impl.SHACLTemplateConstraintImpl;
import org.topbraid.shacl.model.impl.SHACLTemplateImpl;
import org.topbraid.shacl.model.impl.SHACLTemplateScopeImpl;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.SimpleImplementation;

import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.vocabulary.RDF;

public class SHACLFactory {
    
    static {
		init(BuiltinPersonalities.model);
    }

    
	private static void init(Personality<RDFNode> p) {
    	p.add(SHACLArgument.class, new SimpleImplementation(SH.Argument.asNode(), SHACLArgumentImpl.class));
    	p.add(SHACLResult.class, new SimpleImplementation(SH.AbstractResult.asNode(), SHACLResultImpl.class));
    	p.add(SHACLFunction.class, new SimpleImplementation(SH.Function.asNode(), SHACLFunctionImpl.class));
    	p.add(SHACLPropertyConstraint.class, new SimpleImplementation(SH.PropertyConstraint.asNode(), SHACLPropertyConstraintImpl.class));
    	p.add(SHACLShape.class, new SimpleImplementation(SH.Shape.asNode(), SHACLShapeImpl.class));
    	p.add(SHACLNativeConstraint.class, new SimpleImplementation(SH.NativeConstraint.asNode(), SHACLNativeConstraintImpl.class));
    	p.add(SHACLNativeScope.class, new SimpleImplementation(SH.NativeScope.asNode(), SHACLNativeScopeImpl.class));
    	p.add(SHACLTemplate.class, new SimpleImplementation(SH.Template.asNode(), SHACLTemplateImpl.class));
    	p.add(SHACLTemplateCall.class, new SimpleImplementation(SH.Templates.asNode(), SHACLTemplateCallImpl.class));
    	p.add(SHACLTemplateConstraint.class, new SimpleImplementation(SH.TemplateConstraint.asNode(), SHACLTemplateConstraintImpl.class));
    	p.add(SHACLTemplateScope.class, new SimpleImplementation(SH.TemplateScope.asNode(), SHACLTemplateScopeImpl.class));
    	
		FunctionRegistry.get().put(SH.hasShape.getURI(), HasShapeFunction.class);
		PropertyFunctionRegistry.get().put(ScopeContainsPFunction.URI, ScopeContainsPFunction.class);
    }
	
	
	public static SHACLArgument asArgument(RDFNode resource) {
		return resource.as(SHACLArgument.class);
	}
	
	
	public static SHACLFunction asFunction(RDFNode resource) {
		return resource.as(SHACLFunction.class);
	}
	
	
	public static SHACLNativeConstraint asNativeConstraint(RDFNode node) {
		return node.as(SHACLNativeConstraint.class);
	}
	
	
	public static SHACLNativeScope asNativeScope(RDFNode node) {
		return node.as(SHACLNativeScope.class);
	}
	
	
	public static SHACLPropertyConstraint asPropertyConstraint(RDFNode node) {
		return node.as(SHACLPropertyConstraint.class);
	}
	
	
	public static SHACLShape asShape(RDFNode node) {
		return node.as(SHACLShape.class);
	}
	
	
	public static SHACLTemplate asTemplate(RDFNode resource) {
		return resource.as(SHACLTemplate.class);
	}
	
	
	public static SHACLTemplateCall asTemplateCall(RDFNode resource) {
		return resource.as(SHACLTemplateCall.class);
	}
	
	
	public static SHACLTemplateConstraint asTemplateConstraint(RDFNode node) {
		return node.as(SHACLTemplateConstraint.class);
	}
	
	
	public static boolean isNativeConstraint(RDFNode node) {
		return node instanceof Resource && JenaUtil.hasIndirectType((Resource)node, SH.NativeConstraint);
	}
	
	
	public static boolean isNativeScope(RDFNode node) {
		return node instanceof Resource && JenaUtil.hasIndirectType((Resource)node, SH.NativeScope);
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
    
    
	/**
	 * Checks if a given RDFNode represents a template call.
	 * It either needs to be an instance of an instance of sh:Template, or be a typeless blank node
	 * that has an incoming edge via a property such as sh:property, that has a
	 * declared sh:defaultType.
	 * @param node  the node to check
	 * @return true if node is a template call
	 */
	public static boolean isTemplateCall(RDFNode node) {
		if(node instanceof Resource) {
			Resource resource = (Resource) node;
			
			// Return true if this has sh:Template as its metaclass
			for(Resource type : JenaUtil.getTypes(resource)) {
				if(JenaUtil.hasIndirectType(type, SH.Template)) {
					return true;
				}
			}
			
			// If this is a typeless node, check for defaultType of incoming references
			if(!resource.hasProperty(RDF.type)) {
				Resource dt = SHACLUtil.getDefaultTemplateType(resource);
				if(dt != null && JenaUtil.hasIndirectType(dt, SH.Template)) {
					return true;
				}
			}
		}
		return false;
	}
}
