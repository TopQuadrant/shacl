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

package org.topbraid.spin.util;

import java.util.Map;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.spin.model.Command;


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
	
	private boolean thisDeep;
	
	private boolean thisUnbound;
	
	
	public CommandWrapper(Resource source, String text, String label, Statement statement, boolean thisUnbound, boolean thisDeep) {
		this.label = label;
		this.statement = statement;
		this.source = source;
		this.text = text;
		this.thisDeep = thisDeep;
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
	 * Checks if ?this is used in any nested block.
	 * In such cases, ?this cannot be bound via a BGP inside of the query but needs to be
	 * bound in an outside loop
	 * @return true  if ?this is used deep within the query
	 */
	public boolean isThisDeep() {
		return thisDeep;
	}
	
	
	public boolean isThisUnbound() {
		return thisUnbound;
	}
	
	
	public void setTemplateBinding(Map<String,RDFNode> value) {
		this.templateBinding = value;
	}
}
