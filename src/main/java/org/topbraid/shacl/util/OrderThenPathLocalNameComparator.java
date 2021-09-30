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
package org.topbraid.shacl.util;

import java.util.Comparator;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.shacl.vocabulary.SH;

/**
 * A Comparator that uses sh:order triples of the given resources in ascending order (defaulting to 0).
 * If the order is identical, it compares the local names of the sh:path values.
 * 
 * This is commonly used to sort sh:Parameters.
 * 
 * @author Holger Knublauch
 */
public class OrderThenPathLocalNameComparator implements Comparator<Resource> {
	
	private final static OrderThenPathLocalNameComparator singleton = new OrderThenPathLocalNameComparator();
	
	public static OrderThenPathLocalNameComparator get() {
		return singleton;
	}

	
	@Override
	public int compare(Resource subject1, Resource subject2) {
		int byOrder = getOrder(subject1).compareTo(getOrder(subject2));
		if(byOrder != 0) {
			return byOrder;
		}
		else {
			Resource path1 = subject1.getPropertyResourceValue(SH.path);
			Resource path2 = subject2.getPropertyResourceValue(SH.path);
			if(path1 != null && path1.isURIResource() && path2 != null && path2.isURIResource()) {
				return path1.getLocalName().compareTo(path2.getLocalName());
			}
			return 0;
		}
	}
	
	
	private Double getOrder(Resource subject) {
		Statement s = subject.getProperty(SH.order);
		if(s != null && s.getObject().isLiteral() && s.getLiteral().getValue() instanceof Number) {
			return ((Number)s.getLiteral().getValue()).doubleValue();
		}
		return 0.0;
	}
}
