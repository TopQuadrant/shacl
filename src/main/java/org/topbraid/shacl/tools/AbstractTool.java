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
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.jenax.util.SystemTriples;
import org.topbraid.shacl.util.SHACLSystemModel;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.shacl.vocabulary.TOSH;

class AbstractTool {

	private final static String DATA_FILE = "-datafile";
	
	private final static String SHAPES_FILE = "-shapesfile";

	
	private OntDocumentManager dm = new OntDocumentManager();
	
	private OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
	
	
	AbstractTool() {
		
		InputStream shaclTTL = SHACLSystemModel.class.getResourceAsStream("/rdf/shacl.ttl");
		Model shacl = JenaUtil.createMemoryModel();
		shacl.read(shaclTTL, SH.BASE_URI, FileUtils.langTurtle);
		shacl.add(SystemTriples.getVocabularyModel());
		dm.addModel(SH.BASE_URI, shacl);
		
		InputStream dashTTL = SHACLSystemModel.class.getResourceAsStream("/rdf/dash.ttl");
		Model dash = JenaUtil.createMemoryModel();
		dash.read(dashTTL, SH.BASE_URI, FileUtils.langTurtle);
		dm.addModel(DASH.BASE_URI, dash);
		
		InputStream toshTTL = SHACLSystemModel.class.getResourceAsStream("/rdf/tosh.ttl");
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
