package org.topbraid.shacl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.junit.Assert;
import org.junit.Test;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHResult;
import org.topbraid.shacl.vocabulary.SH;


public class ModelTests {

	@Test
	public void testSHValidationReport() {
		Model m = ModelFactory.createDefaultModel();
		m.read(
				new ByteArrayInputStream(
						("@prefix sh: <http://www.w3.org/ns/shacl#> . \n" + 
						"[ a       sh:ValidationReport ; sh:conforms false ; sh:result [ a sh:ValidationResult ; ] ;]").getBytes()
				),
				null,
				Lang.TURTLE.getName()
		);
		List<Resource> reports = new ArrayList<Resource>(JenaUtil.getAllInstances(m.getResource(SH.ValidationReport.getURI())));
		Assert.assertTrue(reports.size() == 1);
		Assert.assertFalse(SHFactory.asValidationReport(reports.get(0)).isConformant());
		List<SHResult> results = new ArrayList<SHResult>();
		SHFactory.asValidationReport(reports.get(0)).getResults().forEach(results::add);
		Assert.assertTrue(results.size() == 1);
		
	}

}
