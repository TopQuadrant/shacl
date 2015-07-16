package org.topbraid.spin.vocabulary;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;


/**
 * Defines RDF resources that are not yet in the corresponding Jena class.
 * 
 * @author Holger Knublauch
 */
public class RDFx {

	public final static Resource HTML = ResourceFactory.createResource(RDF.getURI() + "HTML");

	public final static Resource langString = ResourceFactory.createResource(RDF.getURI() + "langString");

	public final static Resource PlainLiteral = ResourceFactory.createResource(RDF.getURI() + "PlainLiteral");
}
