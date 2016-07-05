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
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHParameter;
import org.topbraid.shacl.model.SHParameterizable;
import org.topbraid.shacl.model.SHParameterizableInstance;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.spin.util.JenaUtil;

public class SHParameterizableInstanceImpl extends SHResourceImpl implements SHParameterizableInstance {
	
	public SHParameterizableInstanceImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public SHParameterizable getParameterizable() {
		Resource type = JenaUtil.getType(this);
		if(type != null) {
			return SHFactory.asParameterizable(type);
		}
		else {
			type = SHACLUtil.getResourceDefaultType(this);
			if(type != null) {
				return SHFactory.asParameterizable(type);
			}
		}
		return null;
	}

	
	@Override
	public Map<String, RDFNode> getParameterMapByVarNames() {
		Map<String,RDFNode> map = new HashMap<String,RDFNode>();
		SHParameterizable template = getParameterizable();
		if(template != null) {
			for(SHParameter arg : template.getParameters()) {
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
		for(SHParameter arg : getParameterizable().getParameters()) {
			Statement s = getProperty(arg.getPredicate());
			if(s != null) {
				bindings.add(arg.getVarName(), s.getObject());
			}
		}
	}
}