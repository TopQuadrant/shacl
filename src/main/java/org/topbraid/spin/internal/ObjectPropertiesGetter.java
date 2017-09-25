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

package org.topbraid.spin.internal;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.spin.model.Element;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.TriplePattern;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.model.visitor.AbstractTriplesVisitor;
import org.topbraid.spin.vocabulary.SPIN;


/**
 * A utility that can be used to find all properties that occur as object
 * in a triple pattern with ?this as subject.  The system also walks into
 * calls to SPIN Functions such as spl:cardinality and SPIN Templates.
 * 
 * @author Holger Knublauch
 */
public class ObjectPropertiesGetter extends AbstractTriplesVisitor {
	
	private Set<Property> properties = new HashSet<Property>();
	
	private Model targetModel;
	
	
	public ObjectPropertiesGetter(Model targetModel, Element element, Map<Property,RDFNode> initialBindings) {
		super(element, initialBindings);
		this.targetModel = targetModel;
	}
	
	
	public Set<Property> getResults() {
		return properties;
	}

	
	@Override
	protected void handleTriplePattern(TriplePattern triplePattern, Map<Property, RDFNode> bindings) {
		boolean valid = false;
		RDFNode subject = triplePattern.getSubject();
		if(SPIN._this.equals(subject)) {
			valid = true;
		}
		else if(bindings != null) {
			Variable var = SPINFactory.asVariable(subject);
			if(var != null) {
				String varName = var.getName();
				for(Property argPredicate : bindings.keySet()) {
					if(varName.equals(argPredicate.getLocalName())) {
						RDFNode b = bindings.get(argPredicate);
						if(SPIN._this.equals(b)) {
							valid = true;
							break;
						}
					}
				}
			}
		}
		
		if(valid) {
			Resource predicate = triplePattern.getPredicate();
			if(predicate != null) {
				Variable variable = SPINFactory.asVariable(predicate);
				if(variable == null) {
					String uri = predicate.getURI();
					if(uri != null) {
						properties.add(targetModel.getProperty(uri));
					}
				}
				else if(bindings != null) {
					String varName = variable.getName();
					for(Property argPredicate : bindings.keySet()) {
						if(varName.equals(argPredicate.getLocalName())) {
							RDFNode b = bindings.get(argPredicate);
							if(b != null && b.isURIResource()) {
								String uri = ((Resource)b).getURI();
								properties.add(targetModel.getProperty(uri));
							}
						}
					}
				}
			}
		}
	}
}
