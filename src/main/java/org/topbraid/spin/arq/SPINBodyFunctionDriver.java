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
package org.topbraid.spin.arq;

import org.topbraid.spin.model.Function;

import org.apache.jena.rdf.model.Resource;


/**
 * A (default) SPINFunctionDriver using spin:body to find an executable
 * body for a SPIN function.
 * 
 * @author Holger Knublauch
 */
public class SPINBodyFunctionDriver implements SPINFunctionDriver {

	@Override
	public SPINFunctionFactory create(Resource spinFunction) {
		return doCreate(spinFunction.as(Function.class));
	}
	
	
	public static SPINFunctionFactory doCreate(Function spinFunction) {
		return new SPINARQFunction(spinFunction);
	}
}
