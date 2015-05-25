package org.topbraid.shacl.arq.functions;

import org.topbraid.shacl.constraints.ResourceConstraintValidator;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.AbstractFunction3;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;

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
				Model results = JenaUtil.createDefaultModel(); 
				Dataset dataset = DatasetImpl.wrap(env.getDataset());
				Resource shape = (Resource) dataset.getDefaultModel().asRDFNode(shapeNode);
				ResourceConstraintValidator.get().addResourceViolations(dataset, shapesGraph, resource.asNode(), shape.asNode(),
						SHACLUtil.getAllConstraintProperties(),
						SH.Error,
						results,
						null);
				return NodeValue.makeBoolean(results.isEmpty());
			}
			finally {
				SHACLRecursionGuard.end(resourceNode, shapeNode);
			}
		}
	}
}
