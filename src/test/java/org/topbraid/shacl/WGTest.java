package org.topbraid.shacl;

import java.io.InputStream;
import java.net.URI;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.topbraid.shacl.vocabulary.MF;
import org.topbraid.shacl.vocabulary.SHT;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.RDF;

public class WGTest extends TestSuite {
	
	public WGTest() throws Exception {
		String baseURI = getClass().getResource("/manifest.ttl").toURI().toString();
		collectTestCases(baseURI);
	}
	
	
    public static Test suite() throws Exception {
        return new WGTest();
    }
	
	
	private void collectTestCases(String baseURI) throws Exception {
		
		InputStream is = new URI(baseURI).toURL().openStream();
		Model model = ModelFactory.createDefaultModel();
		try {
			model.read(is, baseURI, FileUtils.langTurtle);
		}
		catch(Exception ex) {
			System.err.println("ERROR: Cannot load " + baseURI + ": " + ex);
			return;
		}
		
		for(Resource manifest : model.listSubjectsWithProperty(RDF.type, MF.Manifest).toList()) {
			for(Statement includeS : manifest.listProperties(MF.include).toList()) {
				String include = includeS.getResource().getURI();
				collectTestCases(include);
			}
		}

		// TODO: Should only walk through mf:entries
		for(Resource test : model.listSubjectsWithProperty(RDF.type, SHT.MatchNodeShape).toList()) {
			addSupportedTest(new MatchNodeTestClass(test));
		}
		
		// TODO: Other types
	}
	
	
	private void addSupportedTest(AbstractSHACLTestClass test) {
		if(test.isSupported()) {
			addTest(test);
		}
	}
}
