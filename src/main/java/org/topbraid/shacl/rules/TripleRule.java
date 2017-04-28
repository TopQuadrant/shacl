package org.topbraid.shacl.rules;

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionFactory;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

class TripleRule implements Rule {
	
	private NodeExpression object;
	
	private NodeExpression predicate;
	
	private NodeExpression subject;

	
	TripleRule(Resource resource, ShapesGraph shapesGraph) {
		this.object = createNodeExpression(resource, SH.object, shapesGraph);
		this.predicate = createNodeExpression(resource, SH.predicate, shapesGraph);
		this.subject = createNodeExpression(resource, SH.subject, shapesGraph);
	}
	
	
	private NodeExpression createNodeExpression(Resource resource, Property predicate, ShapesGraph shapesGraph) {
		Statement s = resource.getProperty(predicate);
		if(s == null) {
			throw new IllegalArgumentException("Triple rule without " + predicate.getLocalName());
		}
		return NodeExpressionFactory.get().create(s.getObject(), shapesGraph);
	}


	@Override
	public int execute(RuleEngine ruleEngine, List<RDFNode> focusNodes) {
		int added = 0;
		Model inf = ruleEngine.getInferencesModel();
		for(RDFNode focusNode : focusNodes) {
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
								Statement s = inf.createStatement((Resource)subject, predicate, object);
								if(!inf.contains(s)) {
									added++;
									inf.add(s);
								}
							}
						}
					}
				}
			}
		}
		return added;
	}
}
