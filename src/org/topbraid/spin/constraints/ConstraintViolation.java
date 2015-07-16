/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.constraints;

import java.util.Collection;

import org.topbraid.spin.model.TemplateCall;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * An object representing a failure of a SPIN constraint.
 * 
 * @author Holger Knublauch
 */
public class ConstraintViolation {
	
	private Collection<TemplateCall> fixes;
	
	private Resource level;
	
	private String message;
	
	private Collection<SimplePropertyPath> paths;
	
	private Resource root;
	
	private Resource source;
	
	private RDFNode value;
	
	
	/**
	 * Constructs a new ConstraintViolation.
	 * @param root  the root resource of the violation
	 * @param paths  the paths (may be empty)
	 * @param fixes  potential fixes for the violations (may be empty)
	 * @param message  the message explaining the error
	 * @param source  the SPIN Query or template call that has caused this violation
	 *                (may be null)
	 */
	public ConstraintViolation(Resource root, 
				Collection<SimplePropertyPath> paths,
				Collection<TemplateCall> fixes,
				String message,
				Resource source) {
		this.fixes = fixes;
		this.message = message;
		this.root = root;
		this.paths = paths;
		this.source = source;
	}
	
	
	public Collection<TemplateCall> getFixes() {
		return fixes;
	}
	
	
	public Resource getLevel() {
		return level == null ? SPIN.Error : level;
	}
	
	
	public String getMessage() {
		return message;
	}
	
	
	public Collection<SimplePropertyPath> getPaths() {
		return paths;
	}
	

	public Resource getRoot() {
		return root;
	}
	
	
	/**
	 * Gets the SPIN Query or template call that has caused this violation.
	 * @return the source (code should be robust against null values)
	 */
	public Resource getSource() {
		return source;
	}
	
	
	public RDFNode getValue() {
		return value;
	}
	

	/**
	 * Checks if this represents an Error or Fatal.
	 * @return true if Error or Fatal
	 */
	public boolean isError() {
		return SPIN.Error.equals(getLevel()) || SPIN.Fatal.equals(getLevel());
	}
	
	
	public void setLevel(Resource level) {
		this.level = level;
	}
	
	
	public void setValue(RDFNode value) {
		this.value = value;
	}
}
