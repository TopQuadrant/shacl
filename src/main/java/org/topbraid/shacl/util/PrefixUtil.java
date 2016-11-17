package org.topbraid.shacl.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.XSD;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

/**
 * Utilities related to querying and updating prefix declarations based on sh:declare.
 * 
 * @author Holger Knublauch
 */
public class PrefixUtil {
	
	public static Resource addNamespace(Resource ontology, String prefix, String namespace) {
		Resource declaration = ontology.getModel().createResource(SH.PrefixDeclaration);
		ontology.addProperty(SH.declare, declaration);
		declaration.addProperty(SH.prefix, prefix);
		declaration.addProperty(SH.namespace, ResourceFactory.createTypedLiteral(namespace, TypeMapper.getInstance().getSafeTypeByName(XSD.anyURI.getURI())));
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
