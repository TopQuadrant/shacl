package org.topbraid.shacl.rules;

import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.engine.Shape;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Represents a single rule in executable "pre-compiled" form.
 * 
 * @author Holger Knublauch
 */
public abstract class Rule {
	
	private Double order;
	
	private Resource resource;
	
	
	protected Rule(Resource resource) {
		this.resource = resource;
		order = 0.0;
		Statement s = resource.getProperty(SH.order);
		if(s != null && s.getObject().isLiteral()) {
			order = s.getDouble();
		}
	}
	
	/**
	 * Executes this rule, calling <code>ruleEngine.infer()</code> to add triples.
	 * @param ruleEngine  the RuleEngine to operate on
	 * @param focusNodes  the list of focus nodes for this execution
	 */
	public abstract void execute(RuleEngine ruleEngine, List<RDFNode> focusNodes, Shape shape);
	
	
	public String getLabel() {
		Statement s = resource.getProperty(RDFS.label);
		if(s != null && s.getObject().isLiteral()) {
			return s.getString();
		}
		else {
			return null;
		}
	}
	
	
	public String getLabelStart(String type) {
		Double index = getOrder();
		return type + " rule (" + (index == 0 ? "0" : index) + "): ";
	}
	
	
	public Double getOrder() {
		return order;
	}
	
	
	public Resource getResource() {
		return resource;
	}
}
