package org.topbraid.spin.arq.functions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.arq.AbstractFunction;
import org.topbraid.spin.arq.DatasetWithDifferentDefaultModel;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * The base implementation of sh:walkObjects and sh:walkSubjects.
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractWalkFunction extends AbstractFunction {

	@Override
	protected NodeValue exec(Node[] nodes, FunctionEnv env) {
		Node startNode = nodes[0];
		Node predicate = nodes[1];
		Node function = nodes[2];
		Model model = ModelFactory.createModelForGraph(env.getActiveGraph());
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		StringBuffer expression = new StringBuffer("<" + function + ">(?arg1");
		for(int i = 3; i < nodes.length; i++) {
			expression.append(", ");
			expression.append("?");
			String varName = "arg" + (i - 1);
			expression.append(varName);
			if(nodes[i] != null) {
				initialBinding.add(varName, model.asRDFNode(nodes[i]));
			}
		}
		expression.append(")");
		Query query = ARQFactory.get().createExpressionQuery(expression.toString());
		Node result = walkTree(model, 
				DatasetImpl.wrap(env.getDataset()), 
				startNode, predicate, query, initialBinding, new HashSet<Node>());
		if(result != null) {
			return NodeValue.makeNode(result);
		}
		else {
			throw new ExprEvalException("No result");
		}
	}
	
	
	private Node walkTree(Model model, Dataset oldDataset, Node node, Node predicate, 
			Query query, QuerySolution initialBinding, Set<Node> reached) {
		QuerySolutionMap localBinding = new QuerySolutionMap();
		localBinding.addAll(initialBinding);
		localBinding.add("arg1", model.asRDFNode(node));
		Dataset dataset = new DatasetWithDifferentDefaultModel(model, oldDataset);
		QueryExecution qexec = ARQFactory.get().createQueryExecution(query, dataset, localBinding);
		ResultSet rs = qexec.execSelect();
		try {
			if(rs.hasNext()) {
				List<String> resultVars = rs.getResultVars();
				String varName = resultVars.get(0);
				RDFNode resultNode = rs.next().get(varName);
				if(resultNode != null) {
					return resultNode.asNode();
				}
			}
		} 
		finally {
			qexec.close();
		}
		
		// Recurse into parents
		ExtendedIterator<Triple> it = createIterator(model.getGraph(), node, predicate);
		try {
			while(it.hasNext()) {
				Node next = getNext(it.next());
				if((next.isBlank() || next.isURI()) && !reached.contains(next)) {
					reached.add(next);
					Node nextResult = walkTree(model, oldDataset, next, predicate, query, initialBinding, reached);
					if(nextResult != null) {
						return nextResult;
					}
				}
			}
		}
		finally {
			it.close();
		}
		
		return null;
	}


	protected abstract ExtendedIterator<Triple> createIterator(Graph graph, Node node, Node predicate);
	
	
	protected abstract Node getNext(Triple triple);
}
