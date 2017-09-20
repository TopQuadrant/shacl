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
