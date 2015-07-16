package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHACLTemplate;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;

/**
 * Default implementation of SHACLTemplate.
 * 
 * @author Holger Knublauch
 */
public class SHACLTemplateImpl extends SHACLMacroImpl implements SHACLTemplate {
	
	public SHACLTemplateImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public String getLabelTemplate() {
		return JenaUtil.getStringProperty(this, SH.labelTemplate);
	}
}