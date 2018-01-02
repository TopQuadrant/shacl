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
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.model.SHParameter;
import org.topbraid.shacl.model.SHParameterizable;
import org.topbraid.shacl.vocabulary.SH;

public class SHParameterizableImpl extends SHResourceImpl implements SHParameterizable {
	
	public SHParameterizableImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public List<SHParameter> getParameters() {
		List<SHParameter> results = new LinkedList<SHParameter>();
		StmtIterator it = null;
		JenaUtil.setGraphReadOptimization(true);
		try {
			Set<Resource> classes = JenaUtil.getAllSuperClasses(this);
			classes.add(this);
			for(Resource cls : classes) {
				it = cls.listProperties(SH.parameter);
				while(it.hasNext()) {
					Resource param = it.next().getResource();
					results.add(param.as(SHParameter.class));
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
	public Map<String, SHParameter> getParametersMap() {
		Map<String,SHParameter> results = new HashMap<String,SHParameter>();
		for(SHParameter parameter : getParameters()) {
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
	public List<SHParameter> getOrderedParameters() {
		List<SHParameter> results = getParameters();
		boolean orderExists = false;
		for(SHParameter param : results) {
			if(param.hasProperty(SH.order)) {
				orderExists = true;
				break;
			}
		}
		if(orderExists) {
			Collections.sort(results, new Comparator<SHParameter>() {
				@Override
	            public int compare(SHParameter param1, SHParameter param2) {
					Property p1 = param1.getPredicate();
					Property p2 = param2.getPredicate();
					if(p1 != null && p2 != null) {
						Integer index1 = param1.getOrder();
						if(index1 == null) {
							index1 = 0;
						}
						Integer index2 = param2.getOrder();
						if(index2 == null) {
							index2 = 0;
						}
						int comp = index1.compareTo(index2);
						if(comp != 0) {
							return comp;
						}
						else {
							return p1.getLocalName().compareTo(p2.getLocalName());
						}
					}
					else {
						return 0;
					}
				}
			});
		}
		else {
			Collections.sort(results, new Comparator<SHParameter>() {
				@Override
	            public int compare(SHParameter param1, SHParameter param2) {
					Property p1 = param1.getPredicate();
					Property p2 = param2.getPredicate();
					if(p1 != null && p2 != null) {
						return p1.getLocalName().compareTo(p2.getLocalName());
					}
					else {
						return 0;
					}
				}
			});
		}
		return results;
	}


	@Override
	public boolean isOptionalParameter(Property predicate) {
		for(SHParameter param : getParameters()) {
			if(param.hasProperty(SH.path, predicate) && param.hasProperty(SH.optional, JenaDatatypes.TRUE)) {
				return true;
			}
		}
		return false;
	}
}
