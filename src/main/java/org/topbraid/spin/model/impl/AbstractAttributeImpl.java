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

package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.AbstractAttribute;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.vocabulary.SPL;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.vocabulary.RDFS;


public abstract class AbstractAttributeImpl extends AbstractSPINResourceImpl implements AbstractAttribute {
	
	public AbstractAttributeImpl(Node node, EnhGraph eg) {
		super(node, eg);
	}

	
	@Override
    public Property getPredicate() {
		Resource r = getResource(SPL.predicate);
		if(r != null && r.isURIResource()) {
			return new PropertyImpl(r.asNode(), (EnhGraph)r.getModel());
		}
		else {
			return null;
		}
	}


	@Override
    public Resource getValueType() {
		return getResource(SPL.valueType);
	}
	
	
	@Override
    public String getComment() {
		return getString(RDFS.comment);
	}


	@Override
    public void print(PrintContext p) {
		// TODO Auto-generated method stub

	}
}
