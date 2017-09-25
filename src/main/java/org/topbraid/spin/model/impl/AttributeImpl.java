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

import org.topbraid.spin.model.Attribute;
import org.topbraid.spin.vocabulary.SPL;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;


public class AttributeImpl extends AbstractAttributeImpl implements Attribute {
	
	public AttributeImpl(Node node, EnhGraph eg) {
		super(node, eg);
	}


	@Override
    public boolean isOptional() {
		return getMinCount() == 0;
	}


	@Override
    public RDFNode getDefaultValue() {
		return getRDFNode(SPL.defaultValue);
	}


	@Override
    public Integer getMaxCount() {
		Statement s = getProperty(SPL.maxCount);
		if(s != null && s.getObject().isLiteral()) {
			return s.getInt();
		}
		else {
			return null;
		}
	}


	@Override
    public int getMinCount() {
		Statement s = getProperty(SPL.minCount);
		if(s != null && s.getObject().isLiteral()) {
			return s.getInt();
		}
		else {
			return 0;
		}
	}
}
