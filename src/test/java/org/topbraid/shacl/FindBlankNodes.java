/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */
package org.topbraid.shacl;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.tools.BlankNodeFinder;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.shacl.validation.ValidationUtil;


// Utility class to validate some data against a given SHACL file. It also walks through the validation report to find
// blank nodes and add them to the report.
public class FindBlankNodes {

	/**
	 * Loads an example SHACL file and validates all focus nodes against all shapes.
	 */
	public static void main(String[] args)  {
		
		// Load the main data model
		Model dataModel = JenaUtil.createMemoryModel();
		dataModel.read(FindBlankNodes.class.getResourceAsStream("/sh/tests/core/complex/personexample.test.ttl"), "urn:dummy", FileUtils.langTurtle);
		
		// Perform the validation of everything, using the data model
		// also as the shapes model - you may have them separated
		Resource report = ValidationUtil.validateModel(dataModel, dataModel, false);

		Model blankNodes = BlankNodeFinder.findBlankNodes(report.getModel(), dataModel);

		// Add blank nodes to the report

		report.getModel().add(blankNodes);
		
		// Print violations
		System.out.println(ModelPrinter.get().print(report.getModel()));

	}
}