package org.topbraid.shacl.rules;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Triple;
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
import org.topbraid.spin.statistics.SPINStatistics;
import org.topbraid.spin.statistics.SPINStatisticsManager;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;

/**
 * A SHACL Rules engine with a pluggable architecture for different execution languages
 * including Triple rules, SPARQL rules and JavaScript rules.
 * 
 * @author Holger Knublauch
 */
public class RuleEngine implements NodeExpressionContext {
	
	private Dataset dataset;
	
	private Model inferences;
	
	private ProgressMonitor monitor;
	
	private Set<Triple> pending = new HashSet<>();
	
	private Map<Rule,List<Resource>> rule2Conditions = new HashMap<>();
	
	private ShapesGraph shapesGraph;
	
	private URI shapesGraphURI;
	
	private Map<Shape,List<Rule>> shape2Rules = new HashMap<>(); 

	
	public RuleEngine(Dataset dataset, URI shapesGraphURI, ShapesGraph shapesGraph, Model inferences) {
		this.dataset = dataset;
		this.inferences = inferences;
		this.shapesGraph = shapesGraph;
		this.shapesGraphURI = shapesGraphURI;
	}
	
	
	public void executeAll() throws InterruptedException {
		List<Shape> ruleShapes = new ArrayList<Shape>();
		for(Shape shape : shapesGraph.getRootShapes()) {
			if(shape.getShapeResource().hasProperty(SH.rule)) {
				ruleShapes.add(shape);
			}
		}
		if(ruleShapes.isEmpty()) {
			return;
		}
		
		Collections.sort(ruleShapes, new Comparator<Shape>() {
			@Override
			public int compare(Shape shape1, Shape shape2) {
				return shape1.getOrder().compareTo(shape2.getOrder());
			}
		});
		
		String baseMessage = null;
		if(monitor != null) {
			int rules = 0;
			for(Shape shape : ruleShapes) {
				rules += getShapeRules(shape).size();
			}
			baseMessage = "Executing " + rules + " SHACL rules from " + ruleShapes.size() + " shapes";
			monitor.beginTask(baseMessage, rules);
		}
		
		Double oldOrder = ruleShapes.get(0).getOrder();
		for(Shape shape : ruleShapes) {
			if(!oldOrder.equals(shape.getOrder())) {
				oldOrder = shape.getOrder();
				flushPending();
			}
			executeShape(shape, baseMessage);
		}
		flushPending();
	}
	
	
	public void executeShape(Shape shape, String baseMessage) throws InterruptedException {
		
		if(shape.getShapeResource().isDeactivated()) {
			return;
		}
		
		List<Rule> rules = getShapeRules(shape);
		if(rules.isEmpty()) {
			return;
		}
		
		List<RDFNode> targetNodes = SHACLUtil.getTargetNodes(shape.getShapeResource(), dataset);
		if(!targetNodes.isEmpty()) {
			Double oldOrder = rules.get(0).getOrder();
			for(Rule rule : rules) {
				if(monitor != null) {
					if(monitor.isCanceled()) {
						throw new InterruptedException();
					}
					monitor.setTaskName(baseMessage + " (at " + SPINLabels.get().getLabel(shape.getShapeResource()) + " with " + targetNodes.size() + " target nodes)");
					monitor.subTask(rule.toString().replace("\n", " "));
				}
				if(!oldOrder.equals(rule.getOrder())) {
					oldOrder = rule.getOrder();
					flushPending();
				}
				List<Resource> conditions = rule2Conditions.get(rule);
				if(!conditions.isEmpty()) {
					List<RDFNode> filtered = new LinkedList<RDFNode>();
					for(RDFNode targetNode : targetNodes) {
						if(nodeConformsToAllShapes(targetNode, conditions)) {
							filtered.add(targetNode);
						}
					}
					executeRule(rule, filtered, shape);
				}
				else {
					executeRule(rule, targetNodes, shape);
				}
				if(monitor != null) {
					monitor.worked(1);
				}
			}
		}
	}
	
	
	private void executeRule(Rule rule, List<RDFNode> focusNodes, Shape shape) {
		JenaUtil.setGraphReadOptimization(true);
		try {
			if(SPINStatisticsManager.get().isRecording()) {
				long startTime = System.currentTimeMillis();
				rule.execute(this, focusNodes, shape);
				long endTime = System.currentTimeMillis();
				long duration = (endTime - startTime);
				String queryText = rule.toString();
				SPINStatisticsManager.get().add(Collections.singletonList(
						new SPINStatistics(queryText, queryText, duration, startTime, rule.getResource().asNode())));
			}
			else {
				rule.execute(this, focusNodes, shape);
			}
		}
		finally {
			JenaUtil.setGraphReadOptimization(false);
		}
	}


	private List<Rule> getShapeRules(Shape shape) {
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
				RuleLanguage ruleLanguage = RuleLanguages.get().getRuleLanguage(raw);
				if(ruleLanguage == null) {
					throw new IllegalArgumentException("Unsupported SHACL rule type for " + raw);
				}
				Rule rule = ruleLanguage.createRule(raw);
				rules.add(rule);
				List<Resource> conditions = JenaUtil.getResourceProperties(raw, SH.condition);
				rule2Conditions.put(rule, conditions);
			}
		}
		return rules;
	}
	
	
	private int flushPending() {
		int added = 0;
		for(Triple triple : pending) {
			if(!inferences.getGraph().contains(triple)) {
				inferences.add(inferences.asStatement(triple));
				added++;
			}
		}
		pending.clear();
		return added;
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
	
	
	public void infer(Triple triple, Rule rule, Shape shape) {
		pending.add(triple);
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
