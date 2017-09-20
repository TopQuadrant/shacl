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
