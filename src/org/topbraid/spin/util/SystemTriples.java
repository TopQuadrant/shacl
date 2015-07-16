/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.topbraid.spin.vocabulary.RDFx;
import org.topbraid.spin.vocabulary.SP;

import com.hp.hpl.jena.datatypes.xsd.impl.XMLLiteralType;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * Provides access to the RDF/RDFS/OWL system triples.
 * 
 * TopBraid and this API adds some extra triples (such as declaring
 * superclasses for each system class) that make life easier.
 * In order to expose those assumptions to 3rd party APIs, this is
 * part of the (open source) SPIN API.
 * 
 * @author Holger Knublauch
 */
public class SystemTriples {

	private static Model vocabulary;


	private static void ensureSuperClasses(Resource metaClass, Resource superClass) {
		List<Resource> toAdd = collectMissingSuperClasses(metaClass, superClass);
		for (Resource c: toAdd) {
		    vocabulary.add(c, RDFS.subClassOf, superClass);
		}
	}


	private static List<Resource> collectMissingSuperClasses(Resource metaClass,
			Resource superClass) {
		List<Resource> toAdd = new ArrayList<Resource>();
		StmtIterator it = vocabulary.listStatements(null, RDF.type, metaClass);
		while (it.hasNext()) {
			Resource c = it.nextStatement().getSubject();
			if (!c.equals(superClass)) {
				if (c.getProperty(RDFS.subClassOf) == null) {
					toAdd.add(c);
				}
			}
		}
		return toAdd;
	}


	/**
	 * Gets the system ontology (a shared copy).
	 * @return the system ontology
	 */
	public static synchronized Model getVocabularyModel() {
		if (vocabulary == null) {
			vocabulary = JenaUtil.createDefaultModel();
			org.topbraid.spin.util.JenaUtil.initNamespaces(vocabulary.getGraph());
			vocabulary.setNsPrefix("xsd", XSD.getURI());
			InputStream rdfs = SP.class.getResourceAsStream("/etc/rdf-schema.rdf");
			vocabulary.read(rdfs, RDFS.getURI());
			InputStream owl = SP.class.getResourceAsStream("/etc/owl.rdf");
			vocabulary.read(owl, OWL.getURI());
			vocabulary.removeNsPrefix(""); // Otherwise OWL would be default namespace
			ensureSuperClasses(RDFS.Class, RDFS.Resource);
			ensureSuperClasses(OWL.Class, OWL.Thing);
			
			// Remove owl imports rdfs which only causes trouble
			vocabulary.removeAll(null, OWL.imports, null); 
			
			vocabulary.add(OWL.Thing, RDFS.subClassOf, RDFS.Resource);
			vocabulary.add(OWL.inverseOf, RDF.type, OWL.SymmetricProperty);
			vocabulary.add(OWL.equivalentClass, RDF.type, OWL.SymmetricProperty);
			vocabulary.add(OWL.equivalentProperty, RDF.type, OWL.SymmetricProperty);
			vocabulary.add(OWL.equivalentProperty, RDFS.range, RDF.Property);
			vocabulary.add(OWL.differentFrom, RDF.type, OWL.SymmetricProperty);
			vocabulary.add(OWL.sameAs, RDF.type, OWL.SymmetricProperty);
			vocabulary.add(OWL.disjointWith, RDF.type, OWL.SymmetricProperty);
			Resource xml = vocabulary.getResource(XMLLiteralType.theXMLLiteralType.getURI());
			vocabulary.add(xml, RDFS.subClassOf, RDFS.Resource);
			for(String uri : JenaDatatypes.getDatatypeURIs()) {
				Resource r = vocabulary.getResource(uri);
				if (r.getProperty(RDF.type) == null) {
					vocabulary.add(r, RDF.type, RDFS.Datatype);
					vocabulary.add(r, RDFS.subClassOf, RDFS.Literal);
				}
			}
			
			vocabulary.add(RDFx.HTML, RDFS.label, "HTML");
			
			// Triples were formally in OWL 1, but dropped from OWL 2
			vocabulary.add(RDFS.comment, RDF.type, OWL.AnnotationProperty);
			vocabulary.add(RDFS.label, RDF.type, OWL.AnnotationProperty);
			vocabulary.add(RDFS.isDefinedBy, RDF.type, OWL.AnnotationProperty);
			vocabulary.add(RDFS.seeAlso, RDF.type, OWL.AnnotationProperty);
			
			// Add rdfs:labels for XSD types
			for(Resource datatype : vocabulary.listSubjectsWithProperty(RDF.type, RDFS.Datatype).toList()) {
				datatype.addProperty(RDFS.label, datatype.getLocalName());
            }
			vocabulary = JenaUtil.asReadOnlyModel(vocabulary);
		}
		return vocabulary;
	}
}
