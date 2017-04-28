package org.topbraid.shacl.rules;

import org.apache.jena.rdf.model.Resource;

public class TripleRuleLanguage implements RuleLanguage {

	@Override
	public Rule createRule(Resource resource, RuleEngine ruleEngine) {
		return new TripleRule(resource, ruleEngine.getShapesGraph());
	}
}
