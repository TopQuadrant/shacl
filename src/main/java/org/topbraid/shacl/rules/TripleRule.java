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
package org.topbraid.shacl.rules;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.jenax.progress.ProgressMonitor;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.engine.Shape;
import org.topbraid.shacl.expr.AppendContext;
import org.topbraid.shacl.expr.ComplexNodeExpression;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionFactory;
import org.topbraid.shacl.vocabulary.SH;

class TripleRule extends AbstractRule {
	
	private static boolean SPARQL_MODE = false;
	
	private NodeExpression object;
	
	private NodeExpression predicate;
	
	private NodeExpression subject;

	
	TripleRule(Resource resource) {
		super(resource);
		this.object = createNodeExpression(resource, SH.object);
		this.predicate = createNodeExpression(resource, SH.predicate);
		this.subject = createNodeExpression(resource, SH.subject);
	}
	
	
	private NodeExpression createNodeExpression(Resource resource, Property predicate) {
		Statement s = resource.getProperty(predicate);
		if(s == null) {
			throw new IllegalArgumentException("Triple rule without " + predicate.getLocalName());
		}
		return NodeExpressionFactory.get().create(s.getObject());
	}


	@Override
	public void execute(RuleEngine ruleEngine, List<RDFNode> focusNodes, Shape shape) {
		if(SPARQL_MODE) {
			executeSPARQL(ruleEngine, focusNodes, shape);
		}
		else {
			ProgressMonitor monitor = ruleEngine.getProgressMonitor();
			for(RDFNode focusNode : focusNodes) {
				
				if(monitor != null && monitor.isCanceled()) {
					return;
				}

				Iterator<RDFNode> objects = object.eval(focusNode, ruleEngine);
				if(objects.hasNext()) {
					Iterator<RDFNode> subjects = subject.eval(focusNode, ruleEngine);
					if(subjects.hasNext()) {
						Iterator<RDFNode> predicates = predicate.eval(focusNode, ruleEngine);
						if(predicates.hasNext()) {
							while(subjects.hasNext()) {
								RDFNode subjectR = subjects.next();
								if(subjectR.isResource()) {
									Resource subject = (Resource) subjectR;
									while(predicates.hasNext()) {
										RDFNode predicateR = predicates.next();
										if(predicateR.isURIResource()) {
											Property predicate = JenaUtil.asProperty((Resource)predicateR);
											while(objects.hasNext()) {
												RDFNode object = objects.next();
												ruleEngine.infer(Triple.create(subject.asNode(), predicate.asNode(), object.asNode()), this, shape);
											}
										}
									}
								}
							}
						}
					}
				}	
			}
		}
	}
	
	
	private void executeSPARQL(RuleEngine ruleEngine, List<RDFNode> focusNodes, Shape shape) {
		String queryString = getSPARQL();
		Query query = ARQFactory.get().createQuery(ruleEngine.getDataset().getDefaultModel(), queryString);
		QuerySolutionMap binding = new QuerySolutionMap();
		for(RDFNode focusNode : focusNodes) {
			binding.add(SH.thisVar.getVarName(), focusNode);
			Model c;
			try ( QueryExecution qexec = ARQFactory.get().createQueryExecution(query, ruleEngine.getDataset(), binding) ) {
			    c = qexec.execConstruct();
			}
			for(Triple triple : c.getGraph().find(Node.ANY, Node.ANY, Node.ANY).toList()) {
			    ruleEngine.infer(triple, this, shape);
			}
		}
	}


	private String getSPARQL() {
		String label;
		StringBuffer sb = new StringBuffer();
		AppendContext context = new AppendContext(sb);
		sb.append("\nCONSTRUCT {\n");
		sb.append("    ");
		if(subject instanceof ComplexNodeExpression) {
			sb.append("?subject");
		}
		else {
			sb.append(subject);
		}
		sb.append(" ");
		if(predicate instanceof ComplexNodeExpression) {
			sb.append("?predicate");
		}
		else {
			sb.append(predicate);
		}
		sb.append(" ");
		if(object instanceof ComplexNodeExpression) {
			sb.append("?object");
		}
		else {
			sb.append(object);
		}
		sb.append(" .\n}\nWHERE {\n");
		context.increaseIndent();
		if(subject instanceof ComplexNodeExpression) {
			((ComplexNodeExpression)subject).appendSPARQL(context, "subject");
		}
		if(predicate instanceof ComplexNodeExpression) {
			((ComplexNodeExpression)predicate).appendSPARQL(context, "predicate");
		}
		if(object instanceof ComplexNodeExpression) {
			((ComplexNodeExpression)object).appendSPARQL(context, "object");
		}
		context.decreaseIndent();
		sb.append("}");
		label = sb.toString();
		return label;
	}
	
	
	@Override
    public String toString() {
		String label = JenaUtil.getStringProperty(getResource(), RDFS.label);
		if(label == null) {
			label = subject.getFunctionalSyntax() + " - " + predicate.getFunctionalSyntax() + " - " + object.getFunctionalSyntax();
		}
		return getLabelStart("Triple") + label;
	}
}
