package org.topbraid.shacl.rules;

import org.apache.jena.rdf.model.Resource;

public interface RuleLanguage {

	Rule createRule(Resource resource, RuleEngine ruleEngine);
}
