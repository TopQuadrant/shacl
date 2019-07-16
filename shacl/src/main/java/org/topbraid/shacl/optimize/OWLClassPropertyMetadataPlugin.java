package org.topbraid.shacl.optimize;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.jenax.util.JenaNodeUtil;

public class OWLClassPropertyMetadataPlugin implements ClassPropertyMetadata.Plugin {

	@Override
	public void init(ClassPropertyMetadata cpm, Node classNode, Graph graph) {
		ExtendedIterator<Triple> it = graph.find(classNode, RDFS.subClassOf.asNode(), Node.ANY);
		while(it.hasNext()) {
			Node superClass = it.next().getObject();
			if(superClass.isBlank() && graph.contains(superClass, OWL.onProperty.asNode(), cpm.getPredicate())) {
				if(cpm.getLocalRange() == null) {
					Node localRange = JenaNodeUtil.getObject(superClass, OWL.allValuesFrom.asNode(), graph);
					if(localRange != null) {
						cpm.setLocalRange(localRange);
						it.close();
						break;
					}
				}
				if(cpm.getMaxCount() == null) {
					Node maxCountNode = JenaNodeUtil.getObject(superClass, OWL.maxCardinality.asNode(), graph);
					if(maxCountNode == null) {
						maxCountNode = JenaNodeUtil.getObject(superClass, OWL.cardinality.asNode(), graph);
					}
					if(maxCountNode != null && maxCountNode.isLiteral()) {
						Object value = maxCountNode.getLiteralValue();
						if(value instanceof Number) {
							cpm.setMaxCount(((Number) value).intValue());
						}
					}
				}
			}
		}
	}
}
