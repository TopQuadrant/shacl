package org.topbraid.shacl;

import java.io.InputStream;
import java.net.URI;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.topbraid.shacl.constraints.FailureLog;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
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
    	FailureLog.set(new FailureLog() {
			@Override
			public void logFailure(String message) {
				// Suppress
			}
    	});
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
			
			for(Resource list : JenaUtil.getResourceProperties(manifest, MF.entries)) {
				for(RDFNode member : list.as(RDFList.class).asJavaList()) {
					if(!member.isLiteral()) {
						Resource test = (Resource) member;
						if(test.hasProperty(RDF.type, SHT.MatchNodeShape)) {
							addTestIfSupported(new MatchNodeTestClass(test));
						}
						if(test.hasProperty(RDF.type, SHT.Validate)) {
							addTestIfSupported(new ValidateTestClass(test));
						}
						// TODO: Support other types
					}
				}
			}
		}
		
	}
	
	
	private void addTestIfSupported(AbstractSHACLTestClass test) {
		if(test.isSupported()) {
			addTest(test);
		}
	}
}
