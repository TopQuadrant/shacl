/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.vocabulary;

import java.io.InputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.FileUtils;


/**
 * Vocabulary of the SPIN Modeling Vocabulary.
 * 
 * @author Holger Knublauch
 */
public class SPIN {

	public final static String BASE_URI = "http://spinrdf.org/spin";
	
	public final static String NS = BASE_URI + "#";
	
	public final static String PREFIX = "spin";
	
	
	public final static String THIS_VAR_NAME = "this";


	public final static Resource ask = ResourceFactory.createResource(NS + "ask");

	public final static Resource AskTemplate = ResourceFactory.createProperty(NS + "AskTemplate");

	public final static Resource Column = ResourceFactory.createProperty(NS + "Column");

	public final static Resource ConstraintViolation = ResourceFactory.createProperty(NS + "ConstraintViolation");

	public final static Resource ConstraintViolationLevel = ResourceFactory.createProperty(NS + "ConstraintViolationLevel");

	public final static Resource construct = ResourceFactory.createResource(NS + "construct");

	public final static Resource constructViolations = ResourceFactory.createResource(NS + "constructViolations");

	public final static Resource ConstructTemplate = ResourceFactory.createProperty(NS + "ConstructTemplate");

	public final static Resource Error = ResourceFactory.createProperty(NS + "Error");

	public final static Resource eval = ResourceFactory.createResource(NS + "eval");

	public final static Resource Fatal = ResourceFactory.createProperty(NS + "Fatal");

	public final static Resource Function = ResourceFactory.createResource(NS + "Function");

	public final static Resource Functions = ResourceFactory.createResource(NS + "Functions");

	public final static Resource Info = ResourceFactory.createProperty(NS + "Info");

	public final static Resource LibraryOntology = ResourceFactory.createResource(NS + "LibraryOntology");

	public final static Resource MagicProperties = ResourceFactory.createResource(NS + "MagicProperties");

	public final static Resource MagicProperty = ResourceFactory.createResource(NS + "MagicProperty");

	public final static Resource Module = ResourceFactory.createResource(NS + "Module");

	public final static Resource Modules = ResourceFactory.createResource(NS + "Modules");

	public final static Resource Rule = ResourceFactory.createProperty(NS + "Rule");

	public final static Resource RuleProperty = ResourceFactory.createProperty(NS + "RuleProperty");

	public final static Resource select = ResourceFactory.createResource(NS + "select");

	public final static Resource SelectTemplate = ResourceFactory.createProperty(NS + "SelectTemplate");

	public final static Resource TableDataProvider = ResourceFactory.createProperty(NS + "TableDataProvider");

	public final static Resource Template = ResourceFactory.createProperty(NS + "Template");

	public final static Resource Templates = ResourceFactory.createProperty(NS + "Templates");

	public final static Resource UpdateTemplate = ResourceFactory.createProperty(NS + "UpdateTemplate");

	public final static Resource violatesConstraints = ResourceFactory.createResource(NS + "violatesConstraints");

	public final static Resource Warning = ResourceFactory.createProperty(NS + "Warning");

	
	public final static Property abstract_ = ResourceFactory.createProperty(NS + "abstract");
	
	public final static Property body = ResourceFactory.createProperty(NS + "body");

	public final static Property cachable = ResourceFactory.createProperty(NS + "cachable");

	public final static Property column = ResourceFactory.createProperty(NS + "column");

	public final static Property columnIndex = ResourceFactory.createProperty(NS + "columnIndex");

	public final static Property columnWidth = ResourceFactory.createProperty(NS + "columnWidth");

	public final static Property columnType = ResourceFactory.createProperty(NS + "columnType");
	
	public final static Property command = ResourceFactory.createProperty(NS + "command");
	
	public final static Property constraint = ResourceFactory.createProperty(NS + "constraint");
	
	public final static Property constructor = ResourceFactory.createProperty(NS + "constructor");

	public final static Property fix = ResourceFactory.createProperty(NS + "fix");

	public final static Property imports = ResourceFactory.createProperty(NS + "imports");

	public final static Property labelTemplate = ResourceFactory.createProperty(NS + "labelTemplate");

	public final static Property nextRuleProperty = ResourceFactory.createProperty(NS + "nextRuleProperty");

	public final static Property private_ = ResourceFactory.createProperty(NS + "private");

	public final static Property query = ResourceFactory.createProperty(NS + "query");

	public final static Property returnType = ResourceFactory.createProperty(NS + "returnType");
	
	public final static Property rule = ResourceFactory.createProperty(NS + "rule");

	public final static Property rulePropertyMaxIterationCount = ResourceFactory.createProperty(NS + "rulePropertyMaxIterationCount");

	public final static Property symbol = ResourceFactory.createProperty(NS + "symbol");

	public final static Property thisUnbound = ResourceFactory.createProperty(NS + "thisUnbound");
	
	public final static Property violationDetail = ResourceFactory.createProperty(NS + "violationDetail");
	
	public final static Property violationLevel = ResourceFactory.createProperty(NS + "violationLevel");
	
	public final static Property violationPath = ResourceFactory.createProperty(NS + "violationPath");
	
	public final static Property violationRoot = ResourceFactory.createProperty(NS + "violationRoot");
	
	public final static Property violationSource = ResourceFactory.createProperty(NS + "violationSource");
	
	public final static Property violationValue = ResourceFactory.createProperty(NS + "violationValue");
	

	public final static Resource _arg1 = ResourceFactory.createProperty(NS + "_arg1");

	public final static Resource _arg2 = ResourceFactory.createProperty(NS + "_arg2");

	public final static Resource _arg3 = ResourceFactory.createProperty(NS + "_arg3");

	public final static Resource _arg4 = ResourceFactory.createProperty(NS + "_arg4");

	public final static Resource _arg5 = ResourceFactory.createProperty(NS + "_arg5");
	
	public final static Resource _this = ResourceFactory.createResource(NS + "_this");
	
	
	static {
		// Force initialization
		SP.getURI();
	}
	
	
	private static Model model;
	

	/**
	 * Gets a Model with the content of the SPIN namespace, from a file
	 * that is bundled with this API.
	 * @return the namespace Model
	 */
	public static synchronized Model getModel() {
		if(model == null) {
			model = ModelFactory.createDefaultModel();
			InputStream is = SPIN.class.getResourceAsStream("/etc/spin.ttl");
			if(is == null) {
				model.read(SPIN.BASE_URI);
			}
			else {
				model.read(is, "http://dummy", FileUtils.langTurtle);
			}
		}
		return model;
	}
}
