package org.topbraid.shacl.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.topbraid.shacl.util.SHACLSystemModel;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.shacl.vocabulary.TOSH;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.SystemTriples;

class AbstractTool {

	private final static String DATA_FILE = "-datafile";
	
	private final static String SHAPES_FILE = "-shapesfile";

	
	private OntDocumentManager dm = new OntDocumentManager();
	
	private OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
	
	
	AbstractTool() {
		
		InputStream shaclTTL = SHACLSystemModel.class.getResourceAsStream("/etc/shacl.ttl");
		Model shacl = JenaUtil.createMemoryModel();
		shacl.read(shaclTTL, SH.BASE_URI, FileUtils.langTurtle);
		shacl.add(SystemTriples.getVocabularyModel());
		dm.addModel(SH.BASE_URI, shacl);
		
		InputStream dashTTL = SHACLSystemModel.class.getResourceAsStream("/etc/dash.ttl");
		Model dash = JenaUtil.createMemoryModel();
		dash.read(dashTTL, SH.BASE_URI, FileUtils.langTurtle);
		dm.addModel(DASH.BASE_URI, dash);
		
		InputStream toshTTL = SHACLSystemModel.class.getResourceAsStream("/etc/tosh.ttl");
		Model tosh = JenaUtil.createMemoryModel();
		tosh.read(toshTTL, SH.BASE_URI, FileUtils.langTurtle);
		dm.addModel(TOSH.BASE_URI, tosh);
		
		spec.setDocumentManager(dm);
	}
	
	
	protected Model getDataModel(String[] args) throws IOException {
		for(int i = 0; i < args.length - 1; i++) {
			if(DATA_FILE.equals(args[i])) {
				String dataFileName = args[i + 1];
				OntModel dataModel = ModelFactory.createOntologyModel(spec);
				File dataFile = new File(dataFileName);
				dataModel.read(new FileInputStream(dataFile), "urn:x-base", FileUtils.langTurtle);
				return dataModel;
			}
		}
		System.err.println("Missing -datafile, e.g.: -datafile myfile.ttl");
		System.exit(0);
		return null;
	}
	
	
	protected Model getShapesModel(String[] args) throws IOException {
		for(int i = 0; i < args.length - 1; i++) {
			if(SHAPES_FILE.equals(args[i])) {
				String fileName = args[i + 1];
				OntModel model = ModelFactory.createOntologyModel(spec);
				File dataFile = new File(fileName);
				model.read(new FileInputStream(dataFile), "urn:x-base", FileUtils.langTurtle);
				return model;
			}
		}
		return null;
	}
}
