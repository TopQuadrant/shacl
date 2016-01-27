package org.topbraid.shacl.util;

import java.io.StringWriter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.util.FileUtils;

/**
 * A singleton that takes a Jena Model and prints it into a string.
 * Used to create comparable renderings of Models produced by test cases.
 * 
 * @author Holger Knublauch
 */
public class ModelPrinter {

	private static ModelPrinter singleton = new ModelPrinter();
	
	
	public static ModelPrinter get() {
		return singleton;
	}
	
	public static void set(ModelPrinter value) {
		singleton = value;
	}
	
	
	protected RDFWriter createRDFWriter(Model model) {
		return model.getWriter(FileUtils.langTurtle);
	}
	
	
	public String print(Model model) {
		StringWriter writer = new StringWriter();
		RDFWriter w = createRDFWriter(model);
		w.write(model, writer, "http://example.org/random");
		return writer.toString();
	}
}
