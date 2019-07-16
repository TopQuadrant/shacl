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

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Utilities related to querying and updating prefix declarations based on sh:declare.
 * 
 * @author Holger Knublauch
 */
public class PrefixUtil {
	
	public static Resource addNamespace(Resource ontology, String prefix, String namespace) {
		Resource declaration = ontology.getModel().createResource(namespace + SH.PrefixDeclaration.getLocalName(), SH.PrefixDeclaration);
		ontology.addProperty(SH.declare, declaration);
		declaration.addProperty(SH.prefix, prefix);
		declaration.addProperty(SH.namespace, ResourceFactory.createTypedLiteral(namespace, XSDDatatype.XSDanyURI));
		return declaration;
	}
	
	
	public static String getImportedNamespace(Resource ontology, String prefix) {
		return getImportedNamespace(ontology, prefix, new HashSet<Resource>());
	}

	
	private static String getImportedNamespace(Resource ontology, String prefix, Set<Resource> reached) {
		reached.add(ontology);
		
		for(Resource imp : JenaUtil.getResourceProperties(ontology, OWL.imports)) {
			if(!reached.contains(imp)) {
				String ns = getNamespace(imp, prefix);
				if(ns == null) {
					ns = getImportedNamespace(imp, prefix, reached);
				}
				if(ns != null) {
					return ns;
				}
			}
		}
		
		return null;
	}
	
		
	public static String getNamespace(Resource ontology, String prefix) {
		for(Resource declaration : JenaUtil.getResourceProperties(ontology, SH.declare)) {
			if(declaration.hasProperty(SH.prefix, prefix)) {
				return JenaUtil.getStringProperty(declaration, SH.namespace);
			}
		}
		return null;
	}
}
