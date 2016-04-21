package org.topbraid.shacl.model.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.topbraid.shacl.model.SHACLParameter;
import org.topbraid.shacl.model.SHACLParameterizable;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class SHACLParameterizableImpl extends SHACLResourceImpl implements SHACLParameterizable {
	
	public SHACLParameterizableImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public List<SHACLParameter> getParameters() {
		List<SHACLParameter> results = new LinkedList<SHACLParameter>();
		StmtIterator it = null;
		JenaUtil.setGraphReadOptimization(true);
		try {
			Set<Resource> classes = JenaUtil.getAllSuperClasses(this);
			classes.add(this);
			for(Resource cls : classes) {
				it = cls.listProperties(SH.parameter);
				while(it.hasNext()) {
					Resource param = it.next().getResource();
					results.add(param.as(SHACLParameter.class));
				}
			}
		}
		finally {
			if (it != null) {
				it.close();
			}
			JenaUtil.setGraphReadOptimization(false);
		}
		return results;
	}

	
	@Override
	public Map<String, SHACLParameter> getParametersMap() {
		Map<String,SHACLParameter> results = new HashMap<String,SHACLParameter>();
		for(SHACLParameter parameter : getParameters()) {
			Property property = parameter.getPredicate();
			if(property != null) {
				results.put(property.getLocalName(), parameter);
			}
		}
		return results;
	}

	
	@Override
	public String getLabelTemplate() {
		return JenaUtil.getStringProperty(this, SH.labelTemplate);
	}
	
	
	@Override
	public List<SHACLParameter> getOrderedParameters() {
		List<SHACLParameter> results = getParameters();
		Collections.sort(results, new Comparator<SHACLParameter>() {
			@Override
            public int compare(SHACLParameter param1, SHACLParameter param2) {
				Property p1 = param1.getPredicate();
				Property p2 = param2.getPredicate();
				if(p1 != null && p2 != null) {
					Integer index1 = param1.getOrder();
					Integer index2 = param2.getOrder();
					if(index1 != null) {
						if(index2 != null) {
							int comp = index1.compareTo(index2);
							if(comp != 0) {
								return comp;
							}
						}
						else {
							return -1;
						}
					}
					else if(index2 != null) {
						return 1;
					}
					return p1.getLocalName().compareTo(p2.getLocalName());
				}
				else {
					return 0;
				}
			}
		});
		return results;
	}
}