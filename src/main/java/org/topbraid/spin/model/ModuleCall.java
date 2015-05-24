/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import com.hp.hpl.jena.rdf.model.Resource;


/**
 * Abstract base interface of TemplateCall and FunctionCall.
 * 
 * @author Holger Knublauch
 */
public interface ModuleCall extends Resource {
	
	/**
	 * Gets the associated module, i.e. SPIN function or template, derived from the rdf:type.
	 * @return the module
	 */
	Module getModule();
}
