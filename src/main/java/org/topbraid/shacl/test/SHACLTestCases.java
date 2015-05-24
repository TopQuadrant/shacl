package org.topbraid.shacl.test;

import java.util.LinkedList;
import java.util.List;

import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SHACL;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.progress.ProgressMonitor;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * An engine that can execute sh:TestCases.
 * 
 * @author Holger Knublauch
 */
public class SHACLTestCases {

	/**
	 * Performs any constraint checks described by all sh:TestCases in a given Model. 
	 * @param model  the Model containing the test cases
	 * @return the test cases
	 */
	public static List<SHACLConstraintTestFailure> runConstraintTestCases(Model testModel, ProgressMonitor monitor) throws InterruptedException {
		List<SHACLConstraintTestFailure> results = new LinkedList<SHACLConstraintTestFailure>();
		
		for(Resource testCase : JenaUtil.getAllInstances(SHACL.TestCase.inModel(testModel))) {
			
			Resource graph = JenaUtil.getResourceProperty(testCase, SHACL.graph);
			if(graph == null) {
				throw new IllegalArgumentException("Malformed " + SHACL.PREFIX + ":" + SHACL.TestCase.getLocalName() + ": Missing graph");
			}
			
			Model baseModel = ARQFactory.getNamedModel(graph.getURI());
			Model model = SHACLUtil.createIncludesModel(baseModel, graph.getURI());
			
			/*
			Model semanticsModel = SHACLUtil.createShapesModel(model);
			Model expectedModel = createExpectedViolationsModel(testCase);
			Model actualModel = JenaUtil.createMemoryModel();
			actualModel.setNsPrefixes(semanticsModel);
			
			// Perform actual checks
			if(testCase.hasProperty(RDF.type, SHACL.GraphConstraintCheckingTestCase)) {
				Model cm = ModelConstraintValidator.get().validateModel(semanticsModel, false, monitor);
				actualModel.add(cm);
			}
			else if(testCase.hasProperty(RDF.type, SHACL.ResourceConstraintCheckingTestCase)) {
				for(Resource resource : JenaUtil.getResourceProperties(testCase, SHACL.check)) {
					Model cm = ResourceConstraintValidator.get().validateResource(resource.inModel(semanticsModel), null, monitor);
					actualModel.add(cm);
				}
			}
			else {
				throw new IllegalArgumentException("Unexpected TestCase type " + 
						JenaUtil.getType(testCase) + " of " + testCase);
			}
			
			// Compare results graphs
			actualModel.removeAll(null, SHACL.source, (RDFNode)null);
			for(Resource ignore : JenaUtil.getResourceProperties(testCase, SHACL.ignore)) {
				actualModel.removeAll(null, JenaUtil.asProperty(ignore), (RDFNode)null);
				expectedModel.removeAll(null, JenaUtil.asProperty(ignore), (RDFNode)null);
			}
			if(!expectedModel.getGraph().isIsomorphicWith(actualModel.getGraph())) {
				results.add(new SHACLConstraintTestFailure(testCase, expectedModel, actualModel));
			}*/
		}
		
		return results;
	}
	
	
	private static Model createExpectedViolationsModel(Resource testCase) {
		Model result = JenaUtil.createMemoryModel();
		result.setNsPrefixes(testCase.getModel());
		for(Resource violation : JenaUtil.getResourceProperties(testCase, SHACL.violation)) {
			deepCopyTriples(result, violation);
		}
		return result;
	}


	private static void deepCopyTriples(Model result, Resource violation) {
		for(Statement s : violation.listProperties().toList()) {
			result.add(s);
			if(s.getObject().isAnon()) {
				deepCopyTriples(result, s.getResource());
			}
		}
	}
}
