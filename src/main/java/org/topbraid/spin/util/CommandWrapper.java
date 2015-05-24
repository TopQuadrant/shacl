/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.util;

import java.util.Map;

import org.topbraid.spin.model.Command;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;


/**
 * Wraps a (pre-compiled) Jena Query or UpdateRequest with its source SPIN object and
 * a human-readable string representation. 
 * 
 * Also needed to work around the issue of Query.equals/hashCode: Otherwise
 * multiple distinct template calls will be merged into one in HashMaps.
 * 
 * @author Holger Knublauch
 */
public abstract class CommandWrapper {
	
	private String label;
	
	private Resource source;
	
	private Statement statement;
	
	// Used to store the arguments if this wraps a Template call
	private Map<String,RDFNode> templateBinding;
	
	private String text;
	
	private Integer thisDepth;
	
	private boolean thisUnbound;
	
	
	public CommandWrapper(Resource source, String text, String label, Statement statement, boolean thisUnbound, Integer thisDepth) {
		this.label = label;
		this.statement = statement;
		this.source = source;
		this.text = text;
		this.thisDepth = thisDepth;
		this.thisUnbound = thisUnbound;
	}
	
	
	public Map<String,RDFNode> getTemplateBinding() {
		return templateBinding;
	}
	
	
	public String getLabel() {
		return label;
	}
	
	
	public abstract Command getSPINCommand();
	
	
	public Statement getStatement() {
		return statement;
	}
	
	
	/**
	 * Gets the SPIN Query or template call that has created this QueryWrapper. 
	 * @return the source
	 */
	public Resource getSource() {
		return source;
	}
	
	
	public String getText() {
		return text;
	}
	
	
	/**
	 * Gets the maximum depth of ?this in the element tree.
	 * May be null if either not computed (?thisUnbound) or ?this does not exist.
	 * @return the max depth of ?this or null
	 */
	public Integer getThisDepth() {
		return thisDepth;
	}
	
	
	public boolean isThisDeep() {
		return thisDepth != null && thisDepth > 1;
	}
	
	
	public boolean isThisUnbound() {
		return thisUnbound;
	}
	
	
	public void setTemplateBinding(Map<String,RDFNode> value) {
		this.templateBinding = value;
	}
}
