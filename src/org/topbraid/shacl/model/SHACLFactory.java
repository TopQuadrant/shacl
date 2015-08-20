package org.topbraid.shacl.model;

import org.topbraid.shacl.arq.functions.HasShapeFunction;
import org.topbraid.shacl.model.impl.SHACLArgumentImpl;
import org.topbraid.shacl.model.impl.SHACLConstraintViolationImpl;
import org.topbraid.shacl.model.impl.SHACLFunctionImpl;
import org.topbraid.shacl.model.impl.SHACLNativeConstraintImpl;
import org.topbraid.shacl.model.impl.SHACLNativeRuleImpl;
import org.topbraid.shacl.model.impl.SHACLNativeScopeImpl;
import org.topbraid.shacl.model.impl.SHACLPropertyConstraintImpl;
import org.topbraid.shacl.model.impl.SHACLShapeImpl;
import org.topbraid.shacl.model.impl.SHACLTemplateCallImpl;
import org.topbraid.shacl.model.impl.SHACLTemplateConstraintImpl;
import org.topbraid.shacl.model.impl.SHACLTemplateImpl;
import org.topbraid.shacl.model.impl.SHACLTemplateRuleImpl;
import org.topbraid.shacl.model.impl.SHACLTemplateScopeImpl;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.SimpleImplementation;

import com.hp.hpl.jena.enhanced.BuiltinPersonalities;
import com.hp.hpl.jena.enhanced.Personality;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;
import com.hp.hpl.jena.vocabulary.RDF;

public class SHACLFactory {
    
    static {
		init(BuiltinPersonalities.model);
    }

    
	private static void init(Personality<RDFNode> p) {
    	p.add(SHACLArgument.class, new SimpleImplementation(SH.Argument.asNode(), SHACLArgumentImpl.class));
    	p.add(SHACLConstraintViolation.class, new SimpleImplementation(SH.ConstraintViolation.asNode(), SHACLConstraintViolationImpl.class));
    	p.add(SHACLFunction.class, new SimpleImplementation(SH.Function.asNode(), SHACLFunctionImpl.class));
    	p.add(SHACLPropertyConstraint.class, new SimpleImplementation(SH.PropertyConstraint.asNode(), SHACLPropertyConstraintImpl.class));
    	p.add(SHACLShape.class, new SimpleImplementation(SH.Shape.asNode(), SHACLShapeImpl.class));
    	p.add(SHACLNativeConstraint.class, new SimpleImplementation(SH.NativeConstraint.asNode(), SHACLNativeConstraintImpl.class));
    	p.add(SHACLNativeRule.class, new SimpleImplementation(SH.NativeRule.asNode(), SHACLNativeRuleImpl.class));
    	p.add(SHACLNativeScope.class, new SimpleImplementation(SH.NativeScope.asNode(), SHACLNativeScopeImpl.class));
    	p.add(SHACLTemplate.class, new SimpleImplementation(SH.Template.asNode(), SHACLTemplateImpl.class));
    	p.add(SHACLTemplateCall.class, new SimpleImplementation(SH.Templates.asNode(), SHACLTemplateCallImpl.class));
    	p.add(SHACLTemplateConstraint.class, new SimpleImplementation(SH.TemplateConstraint.asNode(), SHACLTemplateConstraintImpl.class));
    	p.add(SHACLTemplateRule.class, new SimpleImplementation(SH.TemplateRule.asNode(), SHACLTemplateRuleImpl.class));
    	p.add(SHACLTemplateScope.class, new SimpleImplementation(SH.TemplateScope.asNode(), SHACLTemplateScopeImpl.class));
    	
		FunctionRegistry.get().put(SH.hasShape.getURI(), HasShapeFunction.class);
    }
	
	
	public static SHACLArgument asArgument(RDFNode resource) {
		return resource.as(SHACLArgument.class);
	}
	
	
	public static SHACLNativeConstraint asNativeConstraint(RDFNode node) {
		return node.as(SHACLNativeConstraint.class);
	}
	
	public static SHACLNativeRule asNativeRule(RDFNode node) {
		return node.as(SHACLNativeRule.class);
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
	
	public static SHACLTemplateRule asTemplateRule(RDFNode node) {
		return node.as(SHACLTemplateRule.class);
	}
	
	
	public static boolean isNativeConstraint(RDFNode node) {
		if(node != null && node.isAnon()) {
			if(((Resource)node).hasProperty(RDF.type, SH.NativeConstraint)) {
				return true;
			}
			if(!((Resource)node).hasProperty(RDF.type)) {
				return SH.NativeConstraint.equals(SHACLUtil.getDefaultTemplateType((Resource)node));
			}
		}
		return false;
	}
	
	public static boolean isNativeRule(RDFNode node) {
		if(node != null && node.isAnon()) {
			if(((Resource)node).hasProperty(RDF.type, SH.NativeRule)) {
				return true;
			}
			if(!((Resource)node).hasProperty(RDF.type)) {
				return SH.NativeRule.equals(SHACLUtil.getDefaultTemplateType((Resource)node));
			}
		}
		return false;
	}
	
	
	public static boolean isNativeScope(RDFNode node) {
		if(node != null && node.isAnon()) {
			if(((Resource)node).hasProperty(RDF.type, SH.NativeScope)) {
				return true;
			}
			if(!((Resource)node).hasProperty(RDF.type)) {
				return SH.NativeScope.equals(SHACLUtil.getDefaultTemplateType((Resource)node));
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
		if(node != null && node.isResource()) {
			Resource resource = (Resource) node;
			
			// Return true if this has sh:Template as its metaclass
			for(Resource type : JenaUtil.getTypes(resource)) {
				if(JenaUtil.hasIndirectType(type, SH.Template)) {
					return true;
				}
			}
			
			// If this is a typeless blank node, check for defaultType of incoming references
			if(resource.isAnon() && !resource.hasProperty(RDF.type)) {
				Resource dt = SHACLUtil.getDefaultTemplateType(resource);
				if(dt != null && !SH.NativeConstraint.equals(dt) && !SH.NativeScope.equals(dt) && !SH.NativeRule.equals(dt)) {
					return true;
				}
			}
		}
		return false;
	}
}
