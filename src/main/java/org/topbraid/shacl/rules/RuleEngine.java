package org.topbraid.shacl.rules;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.shacl.engine.Shape;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.util.OrderComparator;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.validation.ValidationEngineFactory;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.progress.ProgressMonitor;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;

/**
 * A SHACL Rules engine with a pluggable architecture for different execution languages
 * including SPARQL and JavaScript.
 * 
 * @author Holger Knublauch
 */
public class RuleEngine implements NodeExpressionContext {
	
	private Dataset dataset;
	
	private Model inferences;
	
	private ProgressMonitor monitor;
	
	private Map<Rule,List<Resource>> rule2Conditions = new HashMap<>();
	
	private ShapesGraph shapesGraph;
	
	private URI shapesGraphURI;
	
	private Map<Shape,List<Rule>> shape2Rules = new HashMap<>(); 

	
	protected RuleEngine(Dataset dataset, URI shapesGraphURI, ShapesGraph shapesGraph, Model inferences) {
		this.dataset = dataset;
		this.inferences = inferences;
		this.shapesGraph = shapesGraph;
		this.shapesGraphURI = shapesGraphURI;
	}
	
	
	public int executeAll() throws InterruptedException {
		int sum = 0;
		int s = 0;
		do {
			s = 0;
			for(Shape shape : shapesGraph.getRootShapes()) {
				int added = executeShape(shape);
				s += added;
			}
			sum += s;
		}
		while(s > 0);
		return sum;
	}
	
	
	public int executeShape(Shape shape) throws InterruptedException {
		
		if(shape.getShapeResource().isDeactivated()) {
			return 0;
		}
		
		List<Rule> rules = shape2Rules.get(shape);
		if(rules == null) {
			rules = new LinkedList<>();
			shape2Rules.put(shape, rules);
			List<Resource> raws = new LinkedList<Resource>();
			for(Statement s : shape.getShapeResource().listProperties(SH.rule).toList()) {
				if(s.getObject().isResource() && !s.getResource().hasProperty(SH.deactivated, JenaDatatypes.TRUE)) {
					raws.add(s.getResource());
				}
			}
			Collections.sort(raws, OrderComparator.get());
			for(Resource raw : raws) {
				RuleLanguage ruleLanguage = RuleLanguages.get().getRuleLanguage(raw, this);
				if(ruleLanguage == null) {
					throw new IllegalArgumentException("Unsupporte SHACL rule type for " + raw);
				}
				Rule rule = ruleLanguage.createRule(raw, this);
				rules.add(rule);
				List<Resource> conditions = JenaUtil.getResourceProperties(raw, SH.condition);
				rule2Conditions.put(rule, conditions);
			}
		}
		if(rules.isEmpty()) {
			return 0;
		}
		
		
		List<RDFNode> targetNodes = SHACLUtil.getTargetNodes(shape.getShapeResource(), dataset);
		int sum = 0;
		if(!targetNodes.isEmpty()) {
			for(Rule rule : rules) {
				if(monitor != null && monitor.isCanceled()) {
					throw new InterruptedException();
				}
				List<Resource> conditions = rule2Conditions.get(rule);
				if(!conditions.isEmpty()) {
					List<RDFNode> filtered = new LinkedList<RDFNode>();
					for(RDFNode targetNode : targetNodes) {
						if(nodeConformsToAllShapes(targetNode, conditions)) {
							filtered.add(targetNode);
						}
					}
					targetNodes = filtered;
				}
				int added = rule.execute(this, targetNodes);
				sum += added;
			}
		}
		return sum;
	}
	
	
	@Override
	public Dataset getDataset() {
		return dataset;
	}
	
	
	public Model getInferencesModel() {
		return inferences;
	}
	
	
	public ProgressMonitor getProgressMonitor() {
		return monitor;
	}
	
	
	public ShapesGraph getShapesGraph() {
		return shapesGraph;
	}
	
	
	public Model getShapesModel() {
		return dataset.getNamedModel(shapesGraphURI.toString());
	}
	
	
	@Override
	public URI getShapesGraphURI() {
		return shapesGraphURI;
	}
	
	
	private boolean nodeConformsToAllShapes(RDFNode focusNode, Iterable<Resource> shapes) {
		for(Resource shape : shapes) {
			ValidationEngine engine = ValidationEngineFactory.get().create(dataset, shapesGraphURI, shapesGraph, null);
			Resource report = engine.validateNodesAgainstShape(Collections.singletonList(focusNode), shape.asNode());
			if(report.hasProperty(SH.result)) {
				return false;
			}
		}
		return true;
	}
	
	
	public void setProgressMonitor(ProgressMonitor value) {
		this.monitor = value;
	}
}
