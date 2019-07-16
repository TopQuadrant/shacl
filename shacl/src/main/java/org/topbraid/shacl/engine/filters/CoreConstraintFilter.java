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
package org.topbraid.shacl.engine.filters;

import java.util.function.Predicate;

import org.topbraid.shacl.engine.Constraint;

/**
 * Can be used with <code>ShapesGraph.setConstraintFilter</code> to ignore any
 * constraints outside of SHACL Core.
 * 
 * @author Holger Knublauch
 */
public class CoreConstraintFilter implements Predicate<Constraint> {
	
	@Override
	public boolean test(Constraint constraint) {
		return constraint.getComponent().isCore();
	}
}
