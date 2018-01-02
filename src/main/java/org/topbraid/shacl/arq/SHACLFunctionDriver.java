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
package org.topbraid.shacl.arq;

import org.apache.jena.rdf.model.Resource;
import org.topbraid.jenax.functions.DeclarativeFunctionDriver;
import org.topbraid.jenax.functions.DeclarativeFunctionFactory;
import org.topbraid.shacl.model.SHJSFunction;
import org.topbraid.shacl.model.SHSPARQLFunction;
import org.topbraid.shacl.vocabulary.SH;


/**
 * A SPINFunctionDriver for SHACL functions.
 * 
 * This class currently relies on infrastructure from the SPIN API.
 * 
 * @author Holger Knublauch
 */
public class SHACLFunctionDriver implements DeclarativeFunctionDriver {
	
	private static boolean jsPreferred = false;
	
	public static void setJSPreferred(boolean value) {
		jsPreferred = value;
	}

	
	@Override
	public DeclarativeFunctionFactory create(Resource shaclFunction) {
		if(jsPreferred) {
			if(shaclFunction.hasProperty(SH.jsLibrary)) {
				return new SHACLJSARQFunction(shaclFunction.as(SHJSFunction.class));
			}
			else {
				return new SHACLSPARQLARQFunction(shaclFunction.as(SHSPARQLFunction.class));
			}
		}
		else if(shaclFunction.hasProperty(SH.ask) || shaclFunction.hasProperty(SH.select)) {
			return new SHACLSPARQLARQFunction(shaclFunction.as(SHSPARQLFunction.class));
		}
		else {
			return new SHACLJSARQFunction(shaclFunction.as(SHJSFunction.class));
		}
	}
}
