package org.topbraid.shacl.compact;

import java.io.File;
import java.io.FileReader;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.OWL;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.shacl.vocabulary.DASH;

// Not integrated into JUnit tests, but can be manually executed by uncommenting a line in SHACLC.java
class SHACLCTestRunner {

	public void run() {
		File folder = new File("C:\\Users\\Holger\\Documents\\GitHub\\data-shapes\\shacl-compact-syntax\\tests");
		runFolder(folder);
	}
	
	
	private void runFolder(File folder) {
		for(File f : folder.listFiles()) {
			if(f.isDirectory()) {
				runFolder(f);
			}
			else if(f.getName().endsWith(".shaclc")) {
				runFile(f);
			}
		}
	}
	
	
	private void runFile(File compactFile) {
		try {
			Model compactModel = JenaUtil.createMemoryModel();
			compactModel.read(new FileReader(compactFile), "urn:x-base:default", SHACLC.langName);
			compactModel.removeAll(null, OWL.imports, ResourceFactory.createResource(DASH.BASE_URI));

			File turtleFile = new File(compactFile.getParentFile(), compactFile.getName().replaceAll(".shaclc", ".ttl"));
			Model turtleModel = JenaUtil.createMemoryModel();
			turtleModel.read(new FileReader(turtleFile), "urn:x-base:default", FileUtils.langTurtle);
			
			if(compactModel.getGraph().isIsomorphicWith(turtleModel.getGraph())) {
				System.out.println("Passed test " + compactFile);
			}
			else {
				System.err.println("Failed test " + compactFile);
				System.err.println("Turtle:  " + ModelPrinter.get().print(turtleModel));
				System.err.println("Compact: " + ModelPrinter.get().print(compactModel));
			}
		}
		catch(Exception ex) {
			System.err.println("Exception during test " + compactFile + ": " + ex);
			ex.printStackTrace();
		}
	}
}
