package org.topbraid.shacl.arq.functions;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterExtendByVar;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.spin.arq.DatasetWithDifferentDefaultModel;

/**
 * The property function tosh:targetContains.
 * Binds the variable on the right hand side with all focus nodes produced by the
 * SHACL target on the left hand side.
 * 
 * 		(?myTarget ?shapesGraph) tosh:targetContains ?focusNode .
 * 
 * @author Holger Knublauch
 */
public class TargetContainsPFunction extends PropertyFunctionBase {
	
	@Override
	public QueryIterator exec(Binding binding, PropFuncArg argSubject,
			Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {

		argSubject = Substitute.substitute(argSubject, binding);
		argObject = Substitute.substitute(argObject, binding);
		
		if(!argObject.getArg().isVariable()) {
			throw new ExprEvalException("Right hand side of tosh:targetContains must be a variable");
		}
		
		Node targetNode = argSubject.getArgList().get(0);
		Node shapesGraphNode = argSubject.getArgList().get(1);
		
		Model currentModel = ModelFactory.createModelForGraph(execCxt.getActiveGraph());
		Dataset dataset = new DatasetWithDifferentDefaultModel(currentModel, DatasetImpl.wrap(execCxt.getDataset()));

		Model model = dataset.getNamedModel(shapesGraphNode.getURI());
		Resource target = (Resource) model.asRDFNode(targetNode);

		Set<Node> focusNodes = new HashSet<Node>();
		SHACLUtil.addNodesInTarget(target, dataset, focusNodes);
		return new QueryIterExtendByVar(binding, (Var) argObject.getArg(), focusNodes.iterator(), execCxt);
	}
}
