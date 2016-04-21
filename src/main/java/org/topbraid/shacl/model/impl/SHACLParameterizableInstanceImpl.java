package org.topbraid.shacl.model.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.model.SHACLParameter;
import org.topbraid.shacl.model.SHACLParameterizable;
import org.topbraid.shacl.model.SHACLParameterizableInstance;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.spin.util.JenaUtil;

public class SHACLParameterizableInstanceImpl extends SHACLResourceImpl implements SHACLParameterizableInstance {
	
	public SHACLParameterizableInstanceImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public SHACLParameterizable getParameterizable() {
		Resource type = JenaUtil.getType(this);
		if(type != null) {
			return SHACLFactory.asParameterizable(type);
		}
		else {
			type = SHACLUtil.getResourceDefaultType(this);
			if(type != null) {
				return SHACLFactory.asParameterizable(type);
			}
		}
		return null;
	}

	
	@Override
	public Map<String, RDFNode> getParameterMapByVarNames() {
		Map<String,RDFNode> map = new HashMap<String,RDFNode>();
		SHACLParameterizable template = getParameterizable();
		if(template != null) {
			for(SHACLParameter arg : template.getParameters()) {
				Property argProperty = arg.getPredicate();
				if(argProperty != null) {
					String varName = arg.getVarName();
					Statement valueS = getProperty(argProperty);
					if(valueS != null) {
						map.put(varName, valueS.getObject());
					}
				}
			}
		}
		return map;
	}


	@Override
	public void addBindings(QuerySolutionMap bindings) {
		for(SHACLParameter arg : getParameterizable().getParameters()) {
			Statement s = getProperty(arg.getPredicate());
			if(s != null) {
				bindings.add(arg.getVarName(), s.getObject());
			}
		}
	}
}