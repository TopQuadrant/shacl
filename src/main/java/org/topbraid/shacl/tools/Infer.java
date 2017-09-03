package org.topbraid.shacl.tools;

import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileUtils;
import org.topbraid.shacl.rules.RuleUtil;

/**
 * Stand-alone utility to perform inferences based on SHACL rules from a given file.
 *
 * Example arguments:
 * 
 * 		-datafile my.ttl
 * 
 * @author Holger Knublauch
 */
public class Infer extends AbstractTool {
	
	public static void main(String[] args) throws IOException {
		new Infer().run(args);
	}
	
	
	private void run(String[] args) throws IOException {
		Model dataModel = getDataModel(args);
		Model shapesModel = getShapesModel(args);
		if(shapesModel == null) {
			shapesModel = dataModel;
		}
		Model results = RuleUtil.executeRules(dataModel, shapesModel, null, null);
		results.write(System.out, FileUtils.langTurtle);
	}
}
