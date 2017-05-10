package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.shacl.model.SHRule;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

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