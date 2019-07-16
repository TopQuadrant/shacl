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
 * A Comparator that uses sh:order triples of the given resource
 * in ascending order (defaulting to 0).
 * 
 * @author Holger Knublauch
 */
public class OrderComparator implements Comparator<Resource> {
	
	private final static OrderComparator singleton = new OrderComparator();
	
	public static OrderComparator get() {
		return singleton;
	}

	
	@Override
	public int compare(Resource subject1, Resource subject2) {
		return getOrder(subject1).compareTo(getOrder(subject2));
	}
	
	
	private Double getOrder(Resource subject) {
		Statement s = subject.getProperty(SH.order);
		if(s != null && s.getObject().isLiteral() && s.getLiteral().getValue() instanceof Number) {
			return ((Number)s.getLiteral().getValue()).doubleValue();
		}
		return 0.0;
	}
}
