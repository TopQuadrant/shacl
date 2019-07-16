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
package org.topbraid.shacl.validation;

import java.net.URI;

import org.apache.jena.query.Dataset;

/**
 * A singleton used by ResourceConstraintValidator (and thus the tosh:hasShape function)
 * to deliver a default shapes graph if none has been provided in the context.
 * This is to support calling tosh:hasShape outside of a validation engine.
 * 
 * By default, this throws an exception, but within TopBraid products this uses other
 * heuristics to find the most suitable shapes graph.
 * 
 * @author Holger Knublauch
 */
public class DefaultShapesGraphProvider {

	private static DefaultShapesGraphProvider singleton = new DefaultShapesGraphProvider();
	
	public static DefaultShapesGraphProvider get() {
		return singleton;
	}
	
	public static void set(DefaultShapesGraphProvider value) {
		singleton = value;
	}
	
	
	public URI getDefaultShapesGraphURI(Dataset dataset) {
		throw new IllegalArgumentException("Cannot invoke node validation without a shapes graph URI");
	}
}
