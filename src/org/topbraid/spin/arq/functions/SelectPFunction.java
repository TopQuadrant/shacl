package org.topbraid.spin.arq.functions;

import java.util.Collections;
import java.util.List;

import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.arq.DatasetWithDifferentDefaultModel;
import org.topbraid.spin.model.Query;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.TemplateCall;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterConcat;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterExtendByVar;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionBase;
import com.hp.hpl.jena.sparql.util.IterLib;

/**
 * The built-in magic property spin:select.
 * 
 * Executes the query (first argument on the left) and binds the results
 * into the variables on the right.
 * Initial bindings can be specified via pairwise arguments on the right, e.g.
 * 
 * 		(?query "this" ?self) spin:select (?result1 ?result2)
 * 
 * will execute the sp:Select bound to ?query and pre-bind the variable ?this
 * with the current value ?self.
 * 
 * @author Holger Knublauch
 */
public class SelectPFunction extends PropertyFunctionBase {
	
	private final static String NAME = SPIN.PREFIX + ":" + SPIN.select.getLocalName();

	
	@Override
	public QueryIterator exec(Binding binding, PropFuncArg argSubject,
			Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {

		argSubject = Substitute.substitute(argSubject, binding);
		argObject = Substitute.substitute(argObject, binding);

		List<Node> objects = SPINFunctionUtil.getNodes(argObject);
		if(objects.isEmpty()) {
			throw new ExprEvalException(NAME + " must have at least one node on the right side");
		}
		Model model = ModelFactory.createModelForGraph(execCxt.getActiveGraph());
		Resource qot = SPINFunctionUtil.getQueryOrTemplateCall(argSubject, model);
		QuerySolutionMap initialBinding = SPINFunctionUtil.getInitialBinding(SPINFunctionUtil.getNodes(argSubject).toArray(new Node[0]), model);
		Query spinQuery;
		if(SPINFactory.isTemplateCall(qot)) {
			TemplateCall templateCall = SPINFactory.asTemplateCall(qot);
			spinQuery = SPINFactory.asQuery(templateCall.getTemplate().getBody());
			SPINFunctionUtil.addBindingsFromTemplateCall(initialBinding, templateCall);
		}
		else {
			spinQuery = SPINFactory.asQuery(qot);
			if(spinQuery == null) {
				throw new IllegalArgumentException("First argument on the left hand side of spin:select must be a query (e.g. sp:Select) or an instance of a SPIN template");
			}
		}
		com.hp.hpl.jena.query.Query arqQuery = ARQFactory.get().createQuery(spinQuery);
		Dataset dataset = new DatasetWithDifferentDefaultModel(model, DatasetImpl.wrap(execCxt.getDataset()));
		QueryExecution qexec = ARQFactory.get().createQueryExecution(arqQuery, dataset, initialBinding);
		if(arqQuery.isAskType()) {
			return handleAsk(qexec, objects, binding, execCxt);
		}
		else if(arqQuery.isSelectType()) {
			return handleSelect(qexec, objects, binding, execCxt);
		}
		else {
			throw new ExprEvalException(NAME + " can only operate on SELECT or ASK queries");
		}
	}


	private QueryIterator handleAsk(QueryExecution qexec, List<Node> objects,
			Binding binding, ExecutionContext execCxt) {
		boolean result = qexec.execAsk();
		Node resultNode = result ? JenaDatatypes.TRUE.asNode() : JenaDatatypes.FALSE.asNode();
		qexec.close();
		Node firstObject = objects.get(0);
		if(firstObject.isVariable()) {
			return new QueryIterExtendByVar(binding, (Var) firstObject, 
					Collections.singletonList(resultNode).iterator(), 
					execCxt);
		}
		else if(resultNode.matches(firstObject)) {
			// Just continue with existing bindings
			return IterLib.result(binding, execCxt);
		}
		else {
			throw new ExprEvalException("No match");
		}
	}


	private QueryIterator handleSelect(QueryExecution qexec,
			List<Node> objects, Binding binding, ExecutionContext execCxt) {
		ResultSet rs = qexec.execSelect();
		List<String> resultVars = rs.getResultVars();
		QueryIterConcat concat = new QueryIterConcat(execCxt);
		while(rs.hasNext()) {
			QuerySolution qs = rs.next();
			BindingMap bindingMap = new BindingHashMap(binding);
			boolean use = true;
			for(int i = 0; i < resultVars.size() && i < objects.size(); i++) {
				Node object = objects.get(i);
				RDFNode result = qs.get(resultVars.get(i));
				if(object == null) {
					if(result != null) {
						use = false;
						break;
					}
				}
				else if(object.isVariable()) {
					if(result != null) {
						bindingMap.add((Var)object, result.asNode());
					}
				}
				else if(result == null || !object.matches(result.asNode())) {
					use = false;
					break;
				}
			}
			if(use) {
				concat.add(IterLib.result(bindingMap, execCxt));
			}
		}
		qexec.close();
		return concat;
	}
}