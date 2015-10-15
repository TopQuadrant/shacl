package org.topbraid.shacl;

import java.io.InputStream;
import java.net.URI;

import junit.framework.TestCase;

import org.junit.Assert;
import org.topbraid.shacl.arq.SHACLFunctions;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

abstract class AbstractSHACLTestClass extends TestCase {
	
	private Model shaclModel;
	
	private Model shapesModel;

	protected Resource testResource;
	
	
	public AbstractSHACLTestClass(Resource testResource) {
		super("testRun");
		this.testResource = testResource;
		SHACLFunctions.registerFunctions(getSHACLModel());
	}


	protected void compareResults(Model results) {
		String printed = ModelPrinter.get().print(results);
		Statement resultS = testResource.getProperty(MF.result);
		if(resultS == null || JenaDatatypes.TRUE.equals(resultS.getObject())) {
			Assert.assertTrue("Expected no validation results for " + testResource + ", but found: " + results.size() + " triples:\n" + printed, results.isEmpty());
		}
		else if(testResource.hasProperty(MF.result, SHT.Failure)) {
			if(!results.contains(null, RDF.type, DASH.FailureResult)) {
				fail("Validation was expected to produce failure for " + testResource);
			}
		}
		else {
			results.setNsPrefix(SH.PREFIX, SH.NS);
			results.setNsPrefix("rdf", RDF.getURI());
			results.setNsPrefix("rdfs", RDFS.getURI());
			results.removeAll(null, SH.message, (RDFNode)null);
			results.removeAll(null, SH.sourceConstraint, (RDFNode)null);
			results.removeAll(null, SH.sourceShape, (RDFNode)null);
			results.removeAll(null, SH.sourceTemplate, (RDFNode)null);
			Model expected = JenaUtil.createDefaultModel();
			for(Statement s : testResource.listProperties(MF.result).toList()) {
				expected.add(s.getResource().listProperties());
			}
			if(!expected.getGraph().isIsomorphicWith(results.getGraph())) {
				System.out.println("Results for " + testResource);
				System.out.println(ModelPrinter.get().print(results));
				fail("Mismatching validation results for " + testResource + ". Expected " + expected.size() + " triples, found " + results.size() + "\n" + printed);
			}
		}
	}

	
	protected Dataset createDataset() throws Exception {
		Model dataModel = getDataModel();
		if(SHACLUtil.exists(dataModel)) {
			dataModel = SHACLUtil.withDefaultValueTypeInferences(dataModel);
		}
		Dataset result = ARQFactory.get().getDataset(dataModel);
		result.addNamedModel(getShapesGraphURI().toString(), getShapesModel());
		return result;
	}
	
	
	protected Resource getAction() {
		return testResource.getPropertyResourceValue(MF.action);
	}
	
	
	protected Model getDataModel() throws Exception {
		Resource schema = getAction().getPropertyResourceValue(SHT.schema);
		Resource data = getAction().getPropertyResourceValue(SHT.data);
		if(data.equals(schema)) {
			return getShapesModel();
		}
		else {
			return getModelFromAction(SHT.data, SHT.data_format);
		}
	}
	
	
	protected Model getSHACLModel() {
		if(shaclModel == null) {
			shaclModel = JenaUtil.createDefaultModel();
			InputStream is = getClass().getResourceAsStream("/etc/shacl.ttl");
			shaclModel.read(is, SH.BASE_URI, FileUtils.langTurtle);
		}
		return shaclModel;
	}
	
	
	protected URI getShapesGraphURI() {
		return URI.create(getAction().getPropertyResourceValue(SHT.schema).getURI());
	}
	
	
	protected Model getShapesModel() throws Exception {
		if(shapesModel == null) {
			Model model = getModelFromAction(SHT.schema, SHT.schema_format);
			MultiUnion multiUnion = new MultiUnion(new Graph[] {
					model.getGraph(),
					getSHACLModel().getGraph()
			});
			multiUnion.setBaseGraph(model.getGraph());
			shapesModel = ModelFactory.createModelForGraph(multiUnion);
		}
		return shapesModel;
	}
	
	
	private Model getModelFromAction(Property property, Property formatProperty) throws Exception {
		Resource schema = getAction().getPropertyResourceValue(property);
		Resource schemaFormat = getAction().getPropertyResourceValue(formatProperty);
		if(schemaFormat == null) {
			if(schema.getURI().toLowerCase().endsWith(".ttl")) {
				schemaFormat = SHT.TURTLE;
			}
			else if(schema.getURI().toLowerCase().endsWith(".shc")) {
				schemaFormat = SHT.SHACLC;
			}
			else {
				throw new IllegalArgumentException("Cannot determine file format");
			}
		}
		if(SHT.SHACLC.equals(schemaFormat)) {
			throw new UnsupportedTestException();
		}
		
		Model model = JenaUtil.createDefaultModel();
		model.read(new URI(schema.toString()).toURL().openStream(), schema.toString(), FileUtils.langTurtle);
		return model;
	}
	
	
	public boolean isSupported() {
		try {
			getDataModel();
			getShapesModel();
		}
		catch(UnsupportedTestException ex) {
			return false;
		}
		catch(Exception ex) {
			// Ignore for now -> let it bubble up
		}
		return true;
	}
}
