/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.vocabulary;

import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.FileUtils;



/**
 * Vocabulary of the SPIN Standard Modules Library (SPL).
 * 
 * @author Holger Knublauch
 */
public class SPL {

	public final static String BASE_URI = "http://spinrdf.org/spl";
	
	public final static String NS = BASE_URI + "#";
	
	public final static String PREFIX = "spl";
	

    public final static Resource Argument = ResourceFactory.createResource(NS + "Argument");
    
	public final static Resource Attribute = ResourceFactory.createResource(NS + "Attribute");
    
	public final static Resource InferDefaultValue = ResourceFactory.createResource(NS + "InferDefaultValue");
    
	public final static Resource ObjectCountPropertyConstraint = ResourceFactory.createResource(NS + "ObjectCountPropertyConstraint");
    
	public final static Resource primaryKeyProperty = ResourceFactory.createResource(NS + "primaryKeyProperty");
    
	public final static Resource primaryKeyURIStart = ResourceFactory.createResource(NS + "primaryKeyURIStart");
    
	public final static Resource PrimaryKeyPropertyConstraint = ResourceFactory.createResource(NS + "PrimaryKeyPropertyConstraint");
	
	public final static Resource RunTestCases = ResourceFactory.createResource(NS + "RunTestCases");
    
	public final static Resource SPINOverview = ResourceFactory.createResource(NS + "SPINOverview");
    
	public final static Resource TestCase = ResourceFactory.createResource(NS + "TestCase");

    public final static Resource UnionTemplate = ResourceFactory.createResource(NS + "UnionTemplate");
    
    public final static Resource object = ResourceFactory.createResource(NS + "object");
	
	public final static Resource objectCount = ResourceFactory.createResource(NS + "objectCount");
	
	public final static Resource subjectCount = ResourceFactory.createResource(NS + "subjectCount");

	
	public final static Property defaultValue = ResourceFactory.createProperty(NS + "defaultValue");

	public static final Property dynamicEnumRange = ResourceFactory.createProperty(NS + "dynamicEnumRange");
	
	public final static Property hasValue = ResourceFactory.createProperty(NS + "hasValue");
	
	public final static Property maxCount = ResourceFactory.createProperty(NS + "maxCount");
	
	public final static Property minCount = ResourceFactory.createProperty(NS + "minCount");
    
    public final static Property optional = ResourceFactory.createProperty(NS + "optional");
    
	public final static Property predicate = ResourceFactory.createProperty(NS + "predicate");
	
	public final static Property valueType = ResourceFactory.createProperty(NS + "valueType");
	
	static {
		// Force initialization
		SP.getURI();
	}
	
	
	private static Model model;
	

	/**
	 * Gets a Model with the content of the SPL namespace, from a file
	 * that is bundled with this API.
	 * @return the namespace Model
	 */
	public static synchronized Model getModel() {
		if(model == null) {
			model = ModelFactory.createDefaultModel();
			InputStream is = SPL.class.getResourceAsStream("/etc/spl.spin.ttl");
			if(is == null) {
				model.read(SPL.BASE_URI);
			}
			else {
				model.read(is, "http://dummy", FileUtils.langTurtle);
			}
		}
		return model;
	}
}
