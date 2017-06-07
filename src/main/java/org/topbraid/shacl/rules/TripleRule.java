package org.topbraid.shacl.rules;

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
import org.topbraid.shacl.engine.Shape;
import org.topbraid.shacl.expr.AppendContext;
import org.topbraid.shacl.expr.ComplexNodeExpression;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionFactory;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.progress.ProgressMonitor;
import org.topbraid.spin.util.JenaUtil;

class TripleRule extends Rule {
	
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

				List<RDFNode> subjects = subject.eval(focusNode, ruleEngine);
				List<RDFNode> predicates = predicate.eval(focusNode, ruleEngine);
				List<RDFNode> objects = object.eval(focusNode, ruleEngine);
				for(RDFNode subjectR : subjects) {
					if(subjectR.isResource()) {
						Resource subject = (Resource) subjectR;
						for(RDFNode predicateR : predicates) {
							if(predicateR.isURIResource()) {
								Property predicate = JenaUtil.asProperty((Resource)predicateR);
								for(RDFNode object : objects) {
									ruleEngine.infer(Triple.create(subject.asNode(), predicate.asNode(), object.asNode()), this, shape);
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
			QueryExecution qexec = ARQFactory.get().createQueryExecution(query, ruleEngine.getDataset(), binding);
			Model c = qexec.execConstruct();
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
			((ComplexNodeExpression)subject).appendLabel(context, "subject");
		}
		if(predicate instanceof ComplexNodeExpression) {
			((ComplexNodeExpression)predicate).appendLabel(context, "predicate");
		}
		if(object instanceof ComplexNodeExpression) {
			((ComplexNodeExpression)object).appendLabel(context, "object");
		}
		context.decreaseIndent();
		sb.append("}");
		label = sb.toString();
		return label;
	}
	
	
	public String toString() {
		String label = getLabel();
		if(label == null) {
			label = getSPARQL();
		}
		return getLabelStart("Triple") + label;
	}
}
