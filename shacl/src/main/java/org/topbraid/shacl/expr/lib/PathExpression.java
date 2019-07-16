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
package org.topbraid.shacl.expr.lib;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.arq.SHACLPaths;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.expr.AbstractInputExpression;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.expr.NodeExpressionVisitor;
import org.topbraid.shacl.expr.PathEvaluator;
import org.topbraid.shacl.vocabulary.SH;

public class PathExpression extends AbstractInputExpression {
	
	private PathEvaluator eval;
	
	private Resource path;
	
	private String pathString;
	
	
	public PathExpression(RDFNode expr, Resource path, NodeExpression input) {
		super(expr, input);
		this.path = path;
		if(path.isAnon()) {
			pathString = SHACLPaths.getPathString(path);
			Path jenaPath = (Path) SHACLPaths.getJenaPath(pathString, path.getModel());
			eval = new PathEvaluator(jenaPath, expr.getModel());
		}
		else {
			pathString = FmtUtils.stringForRDFNode(path);
			eval = new PathEvaluator(JenaUtil.asProperty(path));
		}
		eval.setInput(input);
	}


	@Override
	public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		try {
			return eval.eval(focusNode, context);
		}
		catch(StackOverflowError ex) {
			throw new IllegalArgumentException("Stack overflow: likely due to recursive dependencies between inferences around SHACL path expression " + this, ex);
		}
	}


	@Override
	public ExtendedIterator<RDFNode> evalReverse(RDFNode valueNode, NodeExpressionContext context) {
		return eval.evalReverse(valueNode, context);
	}


	@Override
	public List<String> getFunctionalSyntaxArguments() {
		List<String> results = new LinkedList<>();
		results.add(pathString);
		NodeExpression input = getInput();
		if(input != null) {
			results.add(input.getFunctionalSyntax());
		}
		return results;
	}
	
	
	@Override
	public Resource getOutputShape(Resource contextShape) {
		if(path.isURIResource()) {
			if(getInput() != null) {
				contextShape = getInput().getOutputShape(contextShape);
			}
			if(contextShape != null) {
				return JenaUtil.getNearest(contextShape, c -> {
					for(Resource ps : JenaUtil.getResourceProperties(c, SH.property)) {
						if(ps.hasProperty(SH.path, path)) {
							Resource node = JenaUtil.getResourceProperty(ps, SH.node);
							if(node != null && node.isURIResource()) {
								return node;
							}
							Resource cls = JenaUtil.getResourceProperty(ps, SH.class_);
							if(cls != null && cls.isURIResource()) {
								return cls;
							}
						}
					}
					return null;
				});
			}
		}
		return null;
	}


	public Resource getPath() {
		return path;
	}
	
	
	@Override
	public String getTypeId() {
		return "path";
	}


	public boolean isMaybeInferred(ShapesGraph shapesGraph) {
		return eval.isMaybeInferred(shapesGraph);
	}


	@Override
	public boolean isReversible(NodeExpressionContext context) {
		return eval.isReversible(context.getShapesGraph());
	}
	
	
	@Override
	public void visit(NodeExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
