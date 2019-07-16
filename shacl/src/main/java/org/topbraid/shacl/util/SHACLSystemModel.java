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

import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileUtils;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.jenax.util.SystemTriples;
import org.topbraid.shacl.arq.SHACLFunctions;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Provides API access to the system graphs needed by SHACL.
 * 
 * This is used by the stand-alone API only, which bundles these files in an etc folder.
 * 
 * @author Holger Knublauch
 */
public class SHACLSystemModel {

	private static Model shaclModel;
	
	
	public static synchronized Model getSHACLModel() {
		if(shaclModel == null) {
			
			shaclModel = JenaUtil.createDefaultModel();
			
			InputStream shaclTTL = SHACLSystemModel.class.getResourceAsStream("/rdf/shacl.ttl");
			shaclModel.read(shaclTTL, SH.BASE_URI, FileUtils.langTurtle);
			
			InputStream dashTTL = SHACLSystemModel.class.getResourceAsStream("/rdf/dash.ttl");
			shaclModel.read(dashTTL, SH.BASE_URI, FileUtils.langTurtle);
			
			InputStream toshTTL = SHACLSystemModel.class.getResourceAsStream("/rdf/tosh.ttl");
			shaclModel.read(toshTTL, SH.BASE_URI, FileUtils.langTurtle);
			
			shaclModel.add(SystemTriples.getVocabularyModel());
			
			SHACLFunctions.registerFunctions(shaclModel);
		}
		return shaclModel;
	}
}
