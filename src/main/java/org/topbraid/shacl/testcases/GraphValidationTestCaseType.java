package org.topbraid.shacl.testcases;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.constraints.ModelConstraintValidator;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.util.JenaUtil;

public class GraphValidationTestCaseType implements TestCaseType {

	public final static List<Property> IGNORED_PROPERTIES = Arrays.asList(new Property[] {
		SH.message, 
		SH.sourceConstraint, 
		SH.sourceShape, 
		SH.sourceConstraintComponent
	});


	@Override
	public Collection<TestCase> getTestCases(Model model, Resource graph) {
		List<TestCase> results = new LinkedList<TestCase>();
		for(Resource resource : JenaUtil.getAllInstances(model.getResource(DASH.GraphValidationTestCase.getURI()))) {
			results.add(new GraphValidationTestCase(graph, resource));
		}
		return results;
	}

	
	private static class GraphValidationTestCase extends TestCase {
		
		GraphValidationTestCase(Resource graph, Resource resource) {
			super(graph, resource);
		}

		@Override
		public void run(Model results) throws Exception {
			
			Model model = SHACLUtil.withDefaultValueTypeInferences(getResource().getModel());

			Dataset dataset = ARQFactory.get().getDataset(model);
			URI shapesGraphURI = SHACLUtil.withShapesGraph(dataset);

			Model actualResults = ModelConstraintValidator.get().validateModel(dataset, shapesGraphURI, null, true, null);
			actualResults.setNsPrefix(SH.PREFIX, SH.NS);
			actualResults.setNsPrefix("rdf", RDF.getURI());
			actualResults.setNsPrefix("rdfs", RDFS.getURI());
			for(Property ignoredProperty : IGNORED_PROPERTIES) {
				actualResults.removeAll(null, ignoredProperty, (RDFNode)null);
			}
			Model expected = JenaUtil.createDefaultModel();
			for(Statement s : getResource().listProperties(DASH.expectedResult).toList()) {
				expected.add(s.getResource().listProperties());
			}
			if(expected.getGraph().isIsomorphicWith(actualResults.getGraph())) {
				createResult(results, DASH.SuccessTestCaseResult);
			}
			else {
				createFailure(results, 
						"Mismatching validation results. Expected " + expected.size() + " triples, found " + actualResults.size());
			}
		}
	}
}
