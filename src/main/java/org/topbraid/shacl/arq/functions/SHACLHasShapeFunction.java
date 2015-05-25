package org.topbraid.shacl.arq.functions;

import org.topbraid.shacl.constraints.ResourceConstraintValidator;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.AbstractFunction3;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;

/**
 * The native implementation of the sh:hasShape function.
 * 
 * @author Holger Knublauch
 */
public class SHACLHasShapeFunction extends AbstractFunction3 {

	@Override
	protected NodeValue exec(Node resourceNode, Node shapeNode, Node shapesGraphNode, FunctionEnv env) {
		
		if(SHACLRecursionGuard.start(resourceNode, shapeNode)) {
			// TODO: Maybe return an error here instead
			return NodeValue.TRUE;
		}
		else {
			
			try {
				Model model = ModelFactory.createModelForGraph(env.getActiveGraph());
				Resource resource = (Resource) model.asRDFNode(resourceNode);
				Resource shapesGraph = (Resource) model.asRDFNode(shapesGraphNode);
				Dataset dataset = DatasetImpl.wrap(env.getDataset());
				Resource shape = (Resource) dataset.getDefaultModel().asRDFNode(shapeNode);
				Model results = ResourceConstraintValidator.get().validateNodeAgainstShape(
						dataset, shapesGraph, resource.asNode(), shape.asNode(), SH.Error, null);
				return NodeValue.makeBoolean(results.isEmpty());
			}
			finally {
				SHACLRecursionGuard.end(resourceNode, shapeNode);
			}
		}
	}
}
