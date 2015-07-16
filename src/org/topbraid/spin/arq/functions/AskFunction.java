package org.topbraid.spin.arq.functions;

import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.arq.AbstractFunction;
import org.topbraid.spin.arq.DatasetWithDifferentDefaultModel;
import org.topbraid.spin.model.Query;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.TemplateCall;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.Function;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.function.FunctionFactory;

/**
 * The built-in function spin:ask.
 * 
 * Can be used to invoke a given sp:Ask query.
 * 
 * @author Holger Knublauch
 */
public class AskFunction extends AbstractFunction implements FunctionFactory {
	
	@Override
	public Function create(String uri) {
		return this;
	}

	
	@Override
	protected NodeValue exec(Node[] nodes, FunctionEnv env) {
		if(nodes.length == 0) {
			throw new ExprEvalException("Missing arguments");
		}
		Model model = ModelFactory.createModelForGraph(env.getActiveGraph());
		QuerySolutionMap initialBinding = SPINFunctionUtil.getInitialBinding(nodes, model);
		Query spinQuery;
		Resource queryOrTemplateCall = model.asRDFNode(nodes[0]).asResource();
		if(SPINFactory.isTemplateCall(queryOrTemplateCall)) {
			TemplateCall templateCall = SPINFactory.asTemplateCall(queryOrTemplateCall);
			spinQuery = SPINFactory.asQuery(templateCall.getTemplate().getBody());
			SPINFunctionUtil.addBindingsFromTemplateCall(initialBinding, templateCall);
		}
		else {
			spinQuery = SPINFactory.asQuery(queryOrTemplateCall);
		}
		Dataset dataset = new DatasetWithDifferentDefaultModel(model, DatasetImpl.wrap(env.getDataset()));
		com.hp.hpl.jena.query.Query arqQuery = ARQFactory.get().createQuery(spinQuery);
		QueryExecution qexec = ARQFactory.get().createQueryExecution(arqQuery, dataset, initialBinding);
		boolean result = qexec.execAsk();
		qexec.close();
		return NodeValue.makeBoolean(result);
	}
}
