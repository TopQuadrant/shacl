package org.topbraid.shacl.rules;

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class ClassificationRuleLanguage implements RuleLanguage {
	
	@Override
	public int execute(Resource rule, RuleEngine engine, List<RDFNode> focusNodes) {
		List<Resource> types = JenaUtil.getResourceProperties(rule, SH.classification);
		int sum = 0;
		Model inf = engine.getInferencesModel();
		for(RDFNode focusNode : focusNodes) {
			if(focusNode instanceof Resource) {
				for(Resource type : types) {
					Statement s = inf.createStatement((Resource)focusNode, RDF.type, type);
					if(!inf.contains(s)) {
						inf.add(s);
						sum++;
					}
				}
			}
		}
		return sum;
	}

	
	@Override
	public Property getKeyProperty() {
		return SH.classification;
	}
}
