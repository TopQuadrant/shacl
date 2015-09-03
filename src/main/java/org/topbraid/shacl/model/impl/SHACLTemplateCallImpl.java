package org.topbraid.shacl.model.impl;

import java.util.HashMap;
import java.util.Map;

import org.topbraid.shacl.model.SHACLArgument;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.model.SHACLTemplate;
import org.topbraid.shacl.model.SHACLTemplateCall;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Default implementation of SHACLTemplateCall.
 * 
 * @author Holger Knublauch
 */
public class SHACLTemplateCallImpl extends SHACLResourceImpl implements SHACLTemplateCall {
	
	public SHACLTemplateCallImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public SHACLTemplate getTemplate() {
		Resource type = JenaUtil.getType(this);
		if(type != null) {
			return SHACLFactory.asTemplate(type);
		}
		else {
			type = SHACLUtil.getDefaultTemplateType(this);
			if(type != null) {
				return SHACLFactory.asTemplate(type);
			}
		}
		return null;
	}

	
	@Override
	public Map<String, RDFNode> getArgumentsMapByVarNames() {
		Map<String,RDFNode> map = new HashMap<String,RDFNode>();
		SHACLTemplate template = getTemplate();
		if(template != null) {
			for(SHACLArgument arg : template.getArguments()) {
				Property argProperty = arg.getPredicate();
				if(argProperty != null) {
					String varName = arg.getVarName();
					Statement valueS = getProperty(argProperty);
					if(valueS != null) {
						map.put(varName, valueS.getObject());
					}
					else if(arg.getDefaultValue() != null) {
						map.put(varName, arg.getDefaultValue());
					}
				}
			}
		}
		return map;
	}


	@Override
	public void addBindings(QuerySolutionMap bindings) {
		for(SHACLArgument arg : getTemplate().getArguments()) {
			Statement s = getProperty(arg.getPredicate());
			if(s != null) {
				bindings.add(arg.getVarName(), s.getObject());
			}
			else {
				RDFNode defaultValue = arg.getDefaultValue();
				if(defaultValue != null) {
					bindings.add(arg.getVarName(), defaultValue);
				}
			}
		}
	}
}