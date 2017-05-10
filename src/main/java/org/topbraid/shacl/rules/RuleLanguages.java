package org.topbraid.shacl.rules;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

/**
 * Global registry of known RuleLanguage instances.
 * 
 * @author Holger Knublauch
 */
public class RuleLanguages {

	private static RuleLanguages singleton = new RuleLanguages();
	
	public static RuleLanguages get() {
		return singleton;
	}
	
	private Map<Resource,RuleLanguage> languages = new HashMap<>();
	
	
	protected RuleLanguages() {
		addLanguage(SH.JSRule, new JSRuleLanguage());
		addLanguage(SH.SPARQLRule, new SPARQLRuleLanguage());
		addLanguage(SH.TripleRule, new TripleRuleLanguage());
	}
	
	
	public void addLanguage(Resource type, RuleLanguage language) {
		languages.put(type, language);
	}
	
	
	public RuleLanguage getRuleLanguage(Resource rule) {
		Resource type = JenaUtil.getType(rule);
		return languages.get(type);
	}
}
