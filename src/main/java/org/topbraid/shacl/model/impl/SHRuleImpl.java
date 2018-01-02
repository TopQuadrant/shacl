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
package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.model.SHRule;
import org.topbraid.shacl.vocabulary.SH;

public class SHRuleImpl extends SHResourceImpl implements SHRule {
	
	public SHRuleImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public RDFNode getSubject() {
		Statement s = getProperty(SH.subject);
		return s != null ? s.getObject() : null;
	}

	
	@Override
	public Resource getPredicate() {
		Statement s = getProperty(SH.predicate);
		return s != null && s.getObject().isResource() ? s.getResource() : null;
	}

	
	@Override
	public RDFNode getObject() {
		Statement s = getProperty(SH.object);
		return s != null ? s.getObject() : null;
	}
	

	@Override
	public boolean isJSRule() {
		return JenaUtil.hasIndirectType(this, SH.JSRule);
	}
	
	
	@Override
	public boolean isSPARQLRule() {
		return JenaUtil.hasIndirectType(this, SH.SPARQLRule);
	}
	
	
	@Override
	public boolean isTripleRule() {
		return JenaUtil.hasIndirectType(this, SH.TripleRule);
	}
}