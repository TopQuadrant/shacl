package org.topbraid.spin.arq.functions;

import java.util.List;

import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.arq.DatasetWithDifferentDefaultModel;
import org.topbraid.spin.model.Query;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.TemplateCall;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
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
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionBase;
import com.hp.hpl.jena.sparql.util.IterLib;

/**
 * The built-in magic property spin:construct.
 * 
 * Executes a given sp:Construct and binds the resulting s,p,o values to
 * the variables on the right hand side.
 * 
 * @author Holger Knublauch
 */
public class ConstructPFunction extends PropertyFunctionBase {
	
	private final static String NAME = SPIN.PREFIX + ":" + SPIN.construct.getLocalName();

	@Override
	public QueryIterator exec(Binding binding, PropFuncArg argSubject,
			Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {

		argSubject = Substitute.substitute(argSubject, binding);
		argObject = Substitute.substitute(argObject, binding);

		List<Node> objects = SPINFunctionUtil.getNodes(argObject);
		if(objects.size() != 3) {
			throw new ExprEvalException(NAME + " must have three nodes on the right side");
		}
		Model model = ModelFactory.createModelForGraph(execCxt.getActiveGraph());
		QuerySolutionMap initialBinding = SPINFunctionUtil.getInitialBinding(SPINFunctionUtil.getNodes(argSubject).toArray(new Node[0]), model);
		Query spinQuery;
		Resource qot = SPINFunctionUtil.getQueryOrTemplateCall(argSubject, model);
		if(SPINFactory.isTemplateCall(qot)) {
			TemplateCall templateCall = SPINFactory.asTemplateCall(qot);
			spinQuery = SPINFactory.asQuery(templateCall.getTemplate().getBody());
			SPINFunctionUtil.addBindingsFromTemplateCall(initialBinding, templateCall);
		}
		else {
			spinQuery = SPINFactory.asQuery(qot);
		}
		com.hp.hpl.jena.query.Query arqQuery = ARQFactory.get().createQuery(spinQuery);
		Dataset dataset = new DatasetWithDifferentDefaultModel(model, DatasetImpl.wrap(execCxt.getDataset()));
		QueryExecution qexec = ARQFactory.get().createQueryExecution(arqQuery, dataset, initialBinding);
		Model result = qexec.execConstruct();
		QueryIterConcat concat = new QueryIterConcat(execCxt);
		for(Triple triple : GraphUtil.findAll(result.getGraph()).toList()) {
			BindingMap bindingMap = new BindingHashMap(binding);
			if(perhapsAdd(objects.get(0), triple.getSubject(), bindingMap)) {
				if(perhapsAdd(objects.get(1), triple.getPredicate(), bindingMap)) {
					if(perhapsAdd(objects.get(2), triple.getObject(), bindingMap)) {
						concat.add(IterLib.result(bindingMap, execCxt));
					}
				}
			}
		}
		qexec.close();
		return concat;
	}
	
	
	private boolean perhapsAdd(Node matchNode, Node tripleNode, BindingMap bindingMap) {
		if(matchNode == null) {
			if(tripleNode != null) {
				return false;
			}
		}
		else if(matchNode.isVariable()) {
			if(tripleNode != null) {
				bindingMap.add((Var)matchNode, tripleNode);
			}
		}
		else if(tripleNode == null || !matchNode.matches(tripleNode)) {
			return false;
		}
		return true;
	}
}
