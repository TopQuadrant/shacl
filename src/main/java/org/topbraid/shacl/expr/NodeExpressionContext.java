package org.topbraid.shacl.expr;

import java.net.URI;

import org.apache.jena.query.Dataset;

public interface NodeExpressionContext {

	Dataset getDataset();
	
	URI getShapesGraphURI();
}
