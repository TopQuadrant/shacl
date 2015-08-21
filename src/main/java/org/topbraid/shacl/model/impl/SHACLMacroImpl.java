package org.topbraid.shacl.model.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topbraid.shacl.model.SHACLArgument;
import org.topbraid.shacl.model.SHACLMacro;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Default implementation of SHACLMacro.
 * 
 * @author Holger Knublauch
 */
public class SHACLMacroImpl extends SHACLClassImpl implements SHACLMacro {

	public SHACLMacroImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public List<SHACLArgument> getArguments() {
		List<SHACLArgument> results = new LinkedList<SHACLArgument>();
		StmtIterator it = null;
		JenaUtil.setGraphReadOptimization(true);
		try {
			Set<Resource> classes = JenaUtil.getAllSuperClasses(this);
			classes.add(this);
			for(Resource cls : classes) {
				it = cls.listProperties(SH.argument);
				while(it.hasNext()) {
					Resource arg = it.next().getResource();
					results.add(arg.as(SHACLArgument.class));
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
	public Map<String, SHACLArgument> getArgumentsMap() {
		Map<String,SHACLArgument> results = new HashMap<String,SHACLArgument>();
		for(SHACLArgument argument : getArguments()) {
			Property property = argument.getPredicate();
			if(property != null) {
				results.put(property.getLocalName(), argument);
			}
		}
		return results;
	}
	
	
	@Override
	public List<SHACLArgument> getOrderedArguments() {
		List<SHACLArgument> results = getArguments();
		Collections.sort(results, new Comparator<SHACLArgument>() {
			public int compare(SHACLArgument arg1, SHACLArgument arg2) {
				Property p1 = arg1.getPredicate();
				Property p2 = arg2.getPredicate();
				if(p1 != null && p2 != null) {
					Integer index1 = arg1.getIndex();
					Integer index2 = arg2.getIndex();
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

	
	@Override
	public String getSPARQL() {
		return JenaUtil.getStringProperty(this, SH.sparql);
	}
}