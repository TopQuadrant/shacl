/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.constraints;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.model.TemplateCall;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.vocabulary.SPIN;


/**
 * An object representing a failure of a SPIN constraint.
 * 
 * @author Holger Knublauch
 */
public class ConstraintViolation {
	
	public static List<ConstraintViolation> shResults2ConstraintViolations(Model resultsModel) {
		List<ConstraintViolation> results = new LinkedList<ConstraintViolation>();
		for(Resource shResult : JenaUtil.getAllInstances(SH.ValidationResult.inModel(resultsModel))) {
			results.add(new ConstraintViolation(shResult));
		}
		return results;
	}
	
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
	
	
	/**
	 * Produces a new ConstraintViolation from a SHACL validation result.
	 * @param shResult
	 */
	public ConstraintViolation(Resource shResult) {
		this.message = JenaUtil.getStringProperty(shResult, SH.message);
		this.root = JenaUtil.getResourceProperty(shResult, SH.focusNode);
		this.paths = new LinkedList<SimplePropertyPath>();
		if(root != null) {
			for(Resource predicate : JenaUtil.getResourceProperties(shResult, SH.predicate)) {
				if(shResult.hasProperty(SH.subject, root)) {
					paths.add(new ObjectPropertyPath(root, JenaUtil.asProperty(predicate)));
				}
				else if(shResult.hasProperty(SH.object, root)) {
					paths.add(new SubjectPropertyPath(root, JenaUtil.asProperty(predicate)));
				}
			}
		}
		if(shResult.hasProperty(SH.severity, SH.Violation)) {
			this.level = SPIN.Error;
		}
		else if(shResult.hasProperty(SH.severity, SH.Warning)) {
			this.level = SPIN.Warning;
		}
		else if(shResult.hasProperty(SH.severity, SH.Info)) {
			this.level = SPIN.Info;
		}
	}
	
	
	public Collection<TemplateCall> getFixes() {
		return fixes == null ? Collections.emptyList() : fixes;
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
