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
package org.topbraid.shacl.model;

import java.util.List;

import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.arq.functions.CheckRegexSyntaxFunction;
import org.topbraid.shacl.arq.functions.EvalExprPFunction;
import org.topbraid.shacl.arq.functions.HasShapeFunction;
import org.topbraid.shacl.arq.functions.IsDeactivatedFunction;
import org.topbraid.shacl.arq.functions.IsInTargetOfFunction;
import org.topbraid.shacl.arq.functions.IsValidForDatatypeFunction;
import org.topbraid.shacl.arq.functions.IsValidLangTagFunction;
import org.topbraid.shacl.arq.functions.TargetContainsMultiFunction;
import org.topbraid.shacl.arq.functions.ValuesPFunction;
import org.topbraid.shacl.model.impl.SHConstraintComponentImpl;
import org.topbraid.shacl.model.impl.SHJSConstraintImpl;
import org.topbraid.shacl.model.impl.SHJSExecutableImpl;
import org.topbraid.shacl.model.impl.SHJSFunctionImpl;
import org.topbraid.shacl.model.impl.SHNodeShapeImpl;
import org.topbraid.shacl.model.impl.SHParameterImpl;
import org.topbraid.shacl.model.impl.SHParameterizableImpl;
import org.topbraid.shacl.model.impl.SHParameterizableInstanceImpl;
import org.topbraid.shacl.model.impl.SHParameterizableTargetImpl;
import org.topbraid.shacl.model.impl.SHPropertyShapeImpl;
import org.topbraid.shacl.model.impl.SHResultImpl;
import org.topbraid.shacl.model.impl.SHRuleImpl;
import org.topbraid.shacl.model.impl.SHSPARQLConstraintImpl;
import org.topbraid.shacl.model.impl.SHSPARQLFunctionImpl;
import org.topbraid.shacl.model.impl.SHSPARQLTargetImpl;
import org.topbraid.shacl.multifunctions.MultiFunctions;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.util.SimpleImplementation;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.shacl.vocabulary.TOSH;

public class SHFactory {
    
    static {
		init(BuiltinPersonalities.model);
    }
    
    
    public static void ensureInited() {
    }

    
	private static void init(Personality<RDFNode> p) {
		p.add(SHConstraintComponent.class, new SimpleImplementation(SH.ConstraintComponent.asNode(), SHConstraintComponentImpl.class));
		p.add(SHJSConstraint.class, new SimpleImplementation(SH.JSConstraint.asNode(), SHJSConstraintImpl.class));
		p.add(SHJSExecutable.class, new SimpleImplementation(SH.JSExecutable.asNode(), SHJSExecutableImpl.class));
		p.add(SHJSFunction.class, new SimpleImplementation(SH.JSFunction.asNode(), SHJSFunctionImpl.class));
    	p.add(SHParameter.class, new SimpleImplementation(SH.Parameter.asNode(), SHParameterImpl.class));
    	p.add(SHParameterizable.class, new SimpleImplementation(SH.Parameterizable.asNode(), SHParameterizableImpl.class));
    	p.add(SHParameterizableInstance.class, new SimpleImplementation(RDFS.Resource.asNode(), SHParameterizableInstanceImpl.class));
    	p.add(SHParameterizableTarget.class, new SimpleImplementation(SH.Target.asNode(), SHParameterizableTargetImpl.class));
    	p.add(SHPropertyShape.class, new SimpleImplementation(SH.PropertyShape.asNode(), SHPropertyShapeImpl.class));
    	p.add(SHResult.class, new SimpleImplementation(SH.AbstractResult.asNode(), SHResultImpl.class));
    	p.add(SHRule.class, new SimpleImplementation(SH.Rule.asNode(), SHRuleImpl.class));
    	p.add(SHNodeShape.class, new SimpleImplementation(SH.NodeShape.asNode(), SHNodeShapeImpl.class));
		p.add(SHSPARQLConstraint.class, new SimpleImplementation(SH.SPARQLConstraint.asNode(), SHSPARQLConstraintImpl.class));
		p.add(SHSPARQLFunction.class, new SimpleImplementation(SH.SPARQLFunction.asNode(), SHSPARQLFunctionImpl.class));
		p.add(SHSPARQLTarget.class, new SimpleImplementation(SH.SPARQLTarget.asNode(), SHSPARQLTargetImpl.class));

		FunctionRegistry.get().put(DASH.isDeactivated.getURI(), IsDeactivatedFunction.class);
		FunctionRegistry.get().put(TOSH.hasShape.getURI(), HasShapeFunction.class);
		FunctionRegistry.get().put(TOSH.isInTargetOf.getURI(), IsInTargetOfFunction.class);
		FunctionRegistry.get().put("http://spinrdf.org/spif#checkRegexSyntax", CheckRegexSyntaxFunction.class);
		FunctionRegistry.get().put("http://spinrdf.org/spif#isValidForDatatype", IsValidForDatatypeFunction.class);
		FunctionRegistry.get().put("http://spinrdf.org/spif#isValidLangTag", IsValidLangTagFunction.class);
		PropertyFunctionRegistry.get().put(TOSH.evalExpr.getURI(), EvalExprPFunction.class);
		PropertyFunctionRegistry.get().put(TOSH.values.getURI(), ValuesPFunction.class);
		MultiFunctions.register(new TargetContainsMultiFunction());
    }
	
	
	public static SHConstraintComponent asConstraintComponent(RDFNode resource) {
		return resource.as(SHConstraintComponent.class);
	}
	
	
	public static SHJSConstraint asJSConstraint(RDFNode node) {
		return node.as(SHJSConstraint.class);
	}
	
	
	public static SHSPARQLFunction asSPARQLFunction(RDFNode resource) {
		return resource.as(SHSPARQLFunction.class);
	}
	
	
	public static SHParameter asParameter(RDFNode resource) {
		return resource.as(SHParameter.class);
	}
	
	
	public static SHParameterizable asParameterizable(RDFNode resource) {
		return resource.as(SHParameterizable.class);
	}
	
	
	public static SHPropertyShape asPropertyShape(RDFNode node) {
		return node.as(SHPropertyShape.class);
	}
	
	
	public static SHNodeShape asNodeShape(RDFNode node) {
		return node.as(SHNodeShape.class);
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
	
	
	public static SHShape asShape(RDFNode node) {
		if(node instanceof Resource && isPropertyShape((Resource)node)) {
			return asPropertyShape(node);
		}
		else if(node instanceof Resource && isParameter((Resource)node)) {
			return asParameter(node);
		}
		else {
			return asNodeShape(node);
		}
	}
	
	
	public static SHParameterizableTarget asParameterizableTarget(RDFNode node) {
		return node.as(SHParameterizableTarget.class);
	}

	
	public static boolean isJSConstraint(RDFNode node) {
		return node instanceof Resource && 
				(JenaUtil.hasIndirectType((Resource)node, SH.JSConstraint) ||
				(!((Resource)node).hasProperty(RDF.type) && node.getModel().contains(null, SH.js, node)));
	}
	
	
	public static boolean isJSTarget(RDFNode node) {
		return node instanceof Resource && JenaUtil.hasIndirectType((Resource)node, SH.JSTarget);
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
	 * Checks if a given node is a NodeShape.
	 * This is just an approximation based on a couple of hard-coded properties.
	 * @param node  the node to test
	 * @return true if node is a NodeShape
	 */
	public static boolean isNodeShape(RDFNode node) {
		if(node instanceof Resource) {
			if(JenaUtil.hasIndirectType((Resource)node, SH.NodeShape)) {
				return true;
			}
			else if(node.isAnon() && !((Resource)node).hasProperty(RDF.type)) {
				if(node.getModel().contains(null, SH.node, node)) {
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
			else if(r.hasProperty(RDF.type, SH.NodeShape) ||
					r.hasProperty(RDF.type, SH.PropertyShape) ||
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
			List<Resource> types = JenaUtil.getTypes(resource);
			for(Resource type : types) {
				if(JenaUtil.hasIndirectType(type, SH.Parameterizable)) {
					return true;
				}
			}
			
			// If this is a typeless node, check for defaultType of incoming references
			if(types.isEmpty()) {
				Resource defaultType = SHACLUtil.getResourceDefaultType(resource);
				if(defaultType != null && JenaUtil.hasIndirectType(defaultType, SH.Parameterizable)) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	public static boolean isPropertyShape(Resource resource) {
		return resource.hasProperty(RDF.type, SH.PropertyShape) ||
				resource.getModel().contains(null, SH.property, resource);
	}
}
