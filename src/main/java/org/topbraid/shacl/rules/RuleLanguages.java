/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */
package org.topbraid.shacl.rules;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Resource;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.vocabulary.SH;

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
