package org.topbraid.shacl.expr;

import java.net.URI;

import org.apache.jena.query.Dataset;
import org.topbraid.shacl.engine.ShapesGraph;

public interface NodeExpressionContext {

	Dataset getDataset();
	
	ShapesGraph getShapesGraph();
	
	URI getShapesGraphURI();
}
