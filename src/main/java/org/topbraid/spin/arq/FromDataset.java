package org.topbraid.spin.arq;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


/**
 * A Dataset that wraps another Dataset but changes its default and
 * named graphs based on the FROM and FROM NAMED clauses of a given
 * Query.
 * 
 * @author Holger Knublauch
 */
public class FromDataset extends DelegatingDataset {
	
	private Set<String> defaultGraphs;
	
	private Model defaultModel;

	private Set<String> namedGraphs;
	
	
	public FromDataset(Dataset delegate, Query query) {
		super(delegate);
		defaultGraphs = new HashSet<String>(query.getGraphURIs());
		namedGraphs = new HashSet<String>(query.getNamedGraphURIs());
	}


	@Override
	public boolean containsNamedModel(String uri) {
		if(namedGraphs.isEmpty()) {
			return true;
		}
		else {
			return namedGraphs.contains(uri);
		}
	}


	@Override
	public Model getDefaultModel() {
		if(defaultGraphs.isEmpty()) {
			return super.getDefaultModel();
		}
		else {
			if(defaultModel == null) {
				if(defaultGraphs.size() == 1) {
					String defaultGraphURI = defaultGraphs.iterator().next();
					defaultModel = getNamedModel(defaultGraphURI);
				}
				else {
					MultiUnion multiUnion = JenaUtil.createMultiUnion();
					for(String baseURI : defaultGraphs) {
						Model model = getNamedModel(baseURI);
						multiUnion.addGraph(model.getGraph());
					}
					defaultModel = ModelFactory.createModelForGraph(multiUnion);
				}
			}
			return defaultModel;
		}
	}


	@Override
	public Iterator<String> listNames() {
		if(namedGraphs.isEmpty()) {
			return super.listNames();
		}
		else {
			return namedGraphs.iterator();
		}
	}
}
