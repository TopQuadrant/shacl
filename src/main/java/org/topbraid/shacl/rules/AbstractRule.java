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
package org.topbraid.shacl.rules;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.shacl.vocabulary.SH;

public abstract class AbstractRule implements Rule {

	private Number order;

	private Resource resource;


	protected AbstractRule(Resource resource) {
		this.resource = resource;
		order = 0;
		Statement s = resource.getProperty(SH.order);
		if(s != null && s.getObject().isLiteral()) {
			order = (Number) s.getLiteral().getValue();
		}
	}


	@Override
	public Node getContextNode() {
		return resource.asNode();
	}


	public String getLabelStart(String type) {
		Number index = getOrder();
		int conditionCount = getResource().listProperties(SH.condition).toList().size();
		return type + " rule (" + (index.doubleValue() == 0 ? "0" : index) +
				(conditionCount > 0 ? (", with " + conditionCount + " conditions") : "") + "): ";
	}


	@Override
    public Number getOrder() {
		return order;
	}


	public Resource getResource() {
		return resource;
	}
}
