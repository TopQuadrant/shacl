package org.topbraid.shacl;

import org.junit.Assert;
import org.topbraid.shacl.constraints.ResourceConstraintValidator;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.MF;
import org.topbraid.shacl.vocabulary.SHACL;
import org.topbraid.shacl.vocabulary.SHT;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class MatchNodeTestClass extends AbstractSHACLTestClass {

	public MatchNodeTestClass(Resource testResource) {
		super(testResource);
	}
	
	
	public void testRun() throws Exception {
		Model shapesModel = getShapesModel();
		Model dataModel = getDataModel();
		Resource action = testResource.getPropertyResourceValue(MF.action);
		Resource focusNode = action.getPropertyResourceValue(SHT.node).inModel(dataModel);
		Resource shape = action.getPropertyResourceValue(SHT.shape).inModel(shapesModel);
		Model results = JenaUtil.createDefaultModel();
		ResourceConstraintValidator.get().addResourceViolations(createDataset(), 
				getShapesGraphURI(), focusNode.asNode(), shape.asNode(),
				SHACLUtil.getAllConstraintProperties(), null, results, null);
		Statement resultS = testResource.getProperty(MF.result);
		if(resultS == null || JenaDatatypes.TRUE.equals(resultS.getObject())) {
			Assert.assertTrue("Expected no validation results, but found: " + results.size() + " triples", results.isEmpty());
		}
		else {
			results.removeAll(null, SHACL.message, (RDFNode)null);
			results.removeAll(null, SHACL.source, (RDFNode)null);
			Model expected = JenaUtil.createDefaultModel();
			for(Statement s : testResource.listProperties(MF.result).toList()) {
				expected.add(s.getResource().listProperties());
			}
			if(!expected.getGraph().isIsomorphicWith(results.getGraph())) {
				fail("Mismatching validation results. Expected " + expected.size() + " triples, found " + results.size());
			}
		}
	}
}
