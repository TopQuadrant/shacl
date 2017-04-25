package org.topbraid.shacl.rules;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.Resource;

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
	
	private List<RuleLanguage> languages = new LinkedList<>();
	
	
	protected RuleLanguages() {
		addLanguage(new JSRuleLanguage());
		addLanguage(new SPARQLConstructRuleLanguage());
		addLanguage(new SPARQLSelectRuleLanguage());
		addLanguage(new ClassificationRuleLanguage());
	}
	
	
	public void addLanguage(RuleLanguage language) {
		languages.add(language);
	}
	
	
	public RuleLanguage getRuleLanguage(Resource rule, RuleEngine engine) {
		for(RuleLanguage language : languages) {
			if(rule.hasProperty(language.getKeyProperty())) {
				return language;
			}
		}
		return null;
	}
}
