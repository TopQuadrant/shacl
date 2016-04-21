package org.topbraid.spin.arq;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.topbraid.spin.util.JenaUtil;


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
	
	
	public FromDataset(Dataset delegate, Query query) throws GraphNotFoundException {
		super(delegate);
		defaultGraphs = new HashSet<String>(query.getGraphURIs());
		namedGraphs = new HashSet<String>(query.getNamedGraphURIs());
		initDefaultModel();
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
		return defaultModel;
	}


	private void initDefaultModel() throws GraphNotFoundException {
		if(defaultGraphs.isEmpty()) {
			defaultModel = super.getDefaultModel();
		}
		else {
			if(defaultGraphs.size() == 1) {
				String defaultGraphURI = defaultGraphs.iterator().next();
				defaultModel = getNamedModel(defaultGraphURI);
				if(defaultModel == null) {
					throw new GraphNotFoundException("Named graph " + defaultGraphURI + " not found");
				}
			}
			else {
				MultiUnion multiUnion = JenaUtil.createMultiUnion();
				for(String graphURI : defaultGraphs) {
					Model model = getNamedModel(graphURI);
					if(model == null) {
						throw new GraphNotFoundException("Named graph " + graphURI + " not found");
					}
					multiUnion.addGraph(model.getGraph());
				}
				defaultModel = ModelFactory.createModelForGraph(multiUnion);
			}
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
