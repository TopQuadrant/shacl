package org.topbraid.shacl.tools;

import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.topbraid.shacl.validation.ValidationUtil;

/**
 * Stand-alone utility to perform constraint validation of a given file.
 *
 * Example arguments:
 * 
 * 		-datafile my.ttl
 * 
 * @author Holger Knublauch
 */
public class Validate extends AbstractTool {
	
	public static void main(String[] args) throws IOException {
		new Validate().run(args);
	}
	
	
	private void run(String[] args) throws IOException {
		Model dataModel = getDataModel(args);
		Model shapesModel = getShapesModel(args);
		if(shapesModel == null) {
			shapesModel = dataModel;
		}
		Resource report = ValidationUtil.validateModel(dataModel, shapesModel, true);
		report.getModel().write(System.out, FileUtils.langTurtle);
	}
}
