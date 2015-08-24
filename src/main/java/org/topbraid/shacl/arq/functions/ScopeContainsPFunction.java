package org.topbraid.shacl.arq.functions;

import java.util.HashSet;
import java.util.Set;

import org.topbraid.shacl.constraints.SHACLException;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.model.SHACLTemplateCall;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterExtendByVar;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionBase;

/**
 * The property function http://topbraid.org/shacl/tbsh#scopeContains.
 * Binds the variable on the right hand side with all focus nodes produced by the
 * SHACL scope on the left hand side.
 * 
 * 		(?myScope ?shapesGraph) tbsh:scopeContains ?focusNode .
 * 
 * @author Holger Knublauch
 */
public class ScopeContainsPFunction extends PropertyFunctionBase {
	
	public final static String URI = "http://topbraid.org/shacl/tbsh#scopeContains";

	
	@Override
	public QueryIterator exec(Binding binding, PropFuncArg argSubject,
			Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {

		argSubject = Substitute.substitute(argSubject, binding);
		argObject = Substitute.substitute(argObject, binding);
		
		if(!argObject.getArg().isVariable()) {
			throw new ExprEvalException("Right hand side " + URI + " must be a variable");
		}
		
		Node scopeNode = argSubject.getArgList().get(0);
		Node shapesGraphNode = argSubject.getArgList().get(1);
		
		Dataset dataset = DatasetImpl.wrap(execCxt.getDataset());

		Model model = dataset.getNamedModel(shapesGraphNode.getURI());
		Resource scope = (Resource) model.asRDFNode(scopeNode);
		Resource type = JenaUtil.getType(scope);

		// TODO: This needs to be generalized to also work with scopes that do not have a SPARQL query
		Query query;
		QuerySolutionMap bindings = new QuerySolutionMap();
		if(type == null || SH.NativeScope.equals(type)) {
			query = getSPARQLWithSelect(scope);
		}
		else {
			query = getSPARQLWithSelect(type);
			SHACLTemplateCall templateCall = SHACLFactory.asTemplateCall(scope);
			templateCall.addBindings(bindings);
		}
		QueryExecution qexec = ARQFactory.get().createQueryExecution(query, dataset, bindings);
		ResultSet rs = qexec.execSelect();
		Set<Node> focusNodes = new HashSet<Node>();
		while(rs.hasNext()) {
			QuerySolution qs = rs.next();
			RDFNode focusNode = qs.get(SH.thisVar.getVarName());
			if(focusNode != null) {
				focusNodes.add(focusNode.asNode());
			}
		}
		qexec.close();
		return new QueryIterExtendByVar(binding, (Var) argObject.getArg(), focusNodes.iterator(), execCxt);
	}
	
	
	private static Query getSPARQLWithSelect(Resource host) {
		String sparql = JenaUtil.getStringProperty(host, SH.sparql);
		if(sparql == null) {
			throw new SHACLException("Missing sh:sparql at " + host);
		}
		try {
			return ARQFactory.get().createQuery(host.getModel(), sparql);
		}
		catch(Exception ex) {
			return ARQFactory.get().createQuery(host.getModel(), "SELECT ?this WHERE {" + sparql + "}");
		}
	}
}
