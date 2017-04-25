package org.topbraid.shacl.rules;

import java.util.List;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public interface RuleLanguage {

	int execute(Resource rule, RuleEngine engine, List<RDFNode> focusNodes);
	
	Property getKeyProperty();
}
