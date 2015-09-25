package org.topbraid.shacl.arq.functions;

import java.net.URI;

import org.topbraid.shacl.constraints.AbstractConstraintValidator;
import org.topbraid.shacl.constraints.FailureLog;
import org.topbraid.shacl.constraints.ResourceConstraintValidator;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.AbstractFunction4;
import org.topbraid.spin.util.JenaDatatypes;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * The native implementation of the sh:hasShape function.
 * 
 * @author Holger Knublauch
 */
public class HasShapeFunction extends AbstractFunction4 {
	
	private static ThreadLocal<Boolean> recursionIsErrorFlag = new ThreadLocal<Boolean>();

	
	@Override
	protected NodeValue exec(Node resourceNode, Node shapeNode, Node shapesGraphNode, Node recursionIsError, FunctionEnv env) {
		Boolean oldFlag = recursionIsErrorFlag.get();
		if(JenaDatatypes.TRUE.asNode().equals(recursionIsError)) {
			recursionIsErrorFlag.set(true);
		}
		try {
			if(SHACLRecursionGuard.start(resourceNode, shapeNode)) {
				if(JenaDatatypes.TRUE.asNode().equals(recursionIsError) || (oldFlag != null && oldFlag)) {
					String message = "Unsupported recursion";
					Model resultsModel = AbstractConstraintValidator.getCurrentResultsModel();
					if(resultsModel != null) {
						Resource failure = resultsModel.createResource(DASH.FailureResult);
						failure.addProperty(SH.message, message);
						failure.addProperty(SH.focusNode, resultsModel.asRDFNode(resourceNode));
						failure.addProperty(SH.sourceShape, resultsModel.asRDFNode(shapeNode));
					}
					FailureLog.get().logFailure(message);
					throw new ExprEvalException("Unsupported recursion");
				}
				else {
					SHACLRecursionGuard.end(resourceNode, shapeNode);
					return NodeValue.TRUE;
				}
			}
			else {
				
				try {
					Model model = ModelFactory.createModelForGraph(env.getActiveGraph());
					RDFNode resource = model.asRDFNode(resourceNode);
					Dataset dataset = DatasetImpl.wrap(env.getDataset());
					Resource shape = (Resource) dataset.getDefaultModel().asRDFNode(shapeNode);
					Model results = doRun(resource, shape, dataset,	shapesGraphNode);
					if(results.contains(null, RDF.type, DASH.FailureResult)) {
						throw new ExprEvalException("Propagating failure from nested shapes");
					}
					return NodeValue.makeBoolean(results.isEmpty());
				}
				finally {
					SHACLRecursionGuard.end(resourceNode, shapeNode);
				}
			}
		}
		finally {
			recursionIsErrorFlag.set(oldFlag);
		}
	}


	protected Model doRun(RDFNode resource, Resource shape, Dataset dataset,
			Node shapesGraphNode) {
		return ResourceConstraintValidator.get().validateNodeAgainstShape(
				dataset, URI.create(shapesGraphNode.getURI()), resource.asNode(), shape.asNode(), SH.Violation, null);
	}
}
