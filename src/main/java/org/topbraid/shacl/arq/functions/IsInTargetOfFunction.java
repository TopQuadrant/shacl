package org.topbraid.shacl.arq.functions;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHShape;
import org.topbraid.spin.arq.AbstractFunction2;

/**
 * The function tosh:isInTargetOf.
 * 
 * @author Holger Knublauch
 */
public class IsInTargetOfFunction extends AbstractFunction2 {

	@Override
	protected NodeValue exec(Node nodeNode, Node shapeNode, FunctionEnv env) {
		Model model = ModelFactory.createModelForGraph(env.getActiveGraph());
		SHShape shape = SHFactory.asShape(model.asRDFNode(shapeNode));
		return NodeValue.makeBoolean(shape.hasTargetNode(model.asRDFNode(nodeNode)));
	}
}
