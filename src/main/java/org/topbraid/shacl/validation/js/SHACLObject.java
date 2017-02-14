package org.topbraid.shacl.validation.js;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.shacl.js.model.JSFactory;
import org.topbraid.shacl.js.model.JSTerm;
import org.topbraid.shacl.validation.ShapesGraph;
import org.topbraid.shacl.validation.ValidationEngineFactory;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class SHACLObject {
	
	private Dataset dataset;
	
	private URI shapesGraphURI;
	
	
	public SHACLObject(URI shapesGraphURI, Dataset dataset) {
		this.shapesGraphURI = shapesGraphURI;
		this.dataset = dataset;
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object[] validateNode(JSTerm node, JSTerm shape) {
		Model shapesModel = dataset.getNamedModel(shapesGraphURI.toString());
		ShapesGraph vsg = new ShapesGraph(shapesModel, null);
		Resource report = ValidationEngineFactory.get().create(dataset, shapesGraphURI, vsg, null).
				validateNodeAgainstShape(node.getNode(), shape.getNode());
		List<Map> results = new LinkedList<>();
		for(Resource instance : JenaUtil.getResourceProperties(report, SH.result)) {
			Map map = new HashMap();
			results.add(map);
			for(Statement s : instance.listProperties().toList()) {
				if(!RDF.type.equals(s.getPredicate())) {
					// TODO: These names are not quite right
					String name = s.getPredicate().getLocalName();
					map.put(name, JSFactory.asJSTerm(s.getObject().asNode()));
				}
			}
		}
		return results.toArray();
	}
}
