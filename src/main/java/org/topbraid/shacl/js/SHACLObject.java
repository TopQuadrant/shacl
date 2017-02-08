package org.topbraid.shacl.js;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.shacl.constraints.NodeConstraintValidator;
import org.topbraid.shacl.js.model.JSFactory;
import org.topbraid.shacl.js.model.JSTerm;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class SHACLObject {
	
	private Dataset dataset;
	
	private URI shapesGraphURI;
	
	
	SHACLObject(URI shapesGraphURI, Dataset dataset) {
		this.shapesGraphURI = shapesGraphURI;
		this.dataset = dataset;
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object[] validateNode(JSTerm node, JSTerm shape, JSGraph dataGraph, JSGraph shapesGraph) {
		Resource report = JenaUtil.createMemoryModel().createResource(SH.ValidationReport);
		new NodeConstraintValidator(report).validateNodeAgainstShape(
				dataset, shapesGraphURI, node.getNode(), shape.getNode(), null, null, null, null);
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
