package org.topbraid.spin.arq;

import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;


/**
 * A QueryIterator produced by a SPIN Magic Property.
 * 
 * This basically walks through the resultset of the body SELECT query.
 * 
 * @author Holger Knublauch
 */
class PFunctionQueryIterator extends QueryIteratorBase {
	
	private Binding parentBinding;
	
	private QueryExecution qexec;

	private ResultSet rs;
	
	private List<String> rvs;
	
	private Map<String,Var> vars;
	
	
	PFunctionQueryIterator(ResultSet rs, QueryExecution qexec, Map<String,Var> vars, Binding parentBinding) {
		this.parentBinding = parentBinding;
		this.qexec = qexec;
		this.rs = rs;
		this.rvs = rs.getResultVars();
		this.vars = vars;
	}


	@Override
	protected void closeIterator() {
		qexec.close();
	}


	@Override
	protected boolean hasNextBinding() {
		return rs.hasNext();
	}


	@Override
	protected Binding moveToNextBinding() {
		QuerySolution s = rs.nextSolution();
		BindingMap result = new BindingHashMap(parentBinding);
		for(String varName : rvs) {
			RDFNode resultNode = s.get(varName);
			if(resultNode != null) {
				Var var = vars.get(varName);
				if(var != null) {
					result.add(var, resultNode.asNode());
				}
			}
		}
		return result;
	}


	public void output(IndentedWriter out, SerializationContext sCxt) {
	}


	@Override
	protected void requestCancel() {
		// TODO: what needs to happen here?
	}
}
