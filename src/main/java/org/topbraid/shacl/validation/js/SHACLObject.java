package org.topbraid.shacl.validation.js;

import java.net.URI;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.js.model.JSTerm;
import org.topbraid.shacl.validation.ShapesGraph;
import org.topbraid.shacl.validation.ValidationEngineFactory;
import org.topbraid.shacl.vocabulary.SH;

public class SHACLObject {
	
	private Dataset dataset;
	
	private URI shapesGraphURI;
	
	
	public SHACLObject(URI shapesGraphURI, Dataset dataset) {
		this.shapesGraphURI = shapesGraphURI;
		this.dataset = dataset;
	}
	
	
	public boolean nodeConformsToShape(JSTerm node, JSTerm shape) {
		Model shapesModel = dataset.getNamedModel(shapesGraphURI.toString());
		ShapesGraph vsg = new ShapesGraph(shapesModel, null);
		Resource report = ValidationEngineFactory.get().create(dataset, shapesGraphURI, vsg, null).
				validateNodeAgainstShape(node.getNode(), shape.getNode());
		return !report.hasProperty(SH.result);
	}
}
