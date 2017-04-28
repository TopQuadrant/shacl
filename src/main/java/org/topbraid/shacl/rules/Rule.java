package org.topbraid.shacl.rules;

import java.util.List;

import org.apache.jena.rdf.model.RDFNode;

public interface Rule {
	
	int execute(RuleEngine ruleEngine, List<RDFNode> focusNodes);
}
