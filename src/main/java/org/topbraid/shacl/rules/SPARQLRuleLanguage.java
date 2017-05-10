package org.topbraid.shacl.rules;

import org.apache.jena.rdf.model.Resource;

public class SPARQLRuleLanguage implements RuleLanguage {

	@Override
	public Rule createRule(Resource resource) {
		return new SPARQLRule(resource);
	}
}
