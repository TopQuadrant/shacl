package org.topbraid.shacl.arq.functions;

import java.util.HashSet;
import java.util.Set;

import org.topbraid.shacl.util.SHACLUtil;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
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

		Set<Node> focusNodes = new HashSet<Node>();
		SHACLUtil.addNodesInScope(scope, dataset, focusNodes);
		return new QueryIterExtendByVar(binding, (Var) argObject.getArg(), focusNodes.iterator(), execCxt);
	}
}
