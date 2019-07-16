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
package org.topbraid.shacl.validation.js;

import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.ConstraintExecutor;
import org.topbraid.shacl.validation.ValidationLanguage;
import org.topbraid.shacl.vocabulary.SH;

public class JSValidationLanguage implements ValidationLanguage {

	private final static JSValidationLanguage singleton = new JSValidationLanguage();
	
	public static JSValidationLanguage get() {
		return singleton;
	}

	
	@Override
	public boolean canExecute(Constraint constraint) {
		return constraint.getComponent().getValidator(SH.JSValidator, constraint.getContext()) != null;
	}

	
	@Override
	public ConstraintExecutor createExecutor(Constraint constraint) {
		return new JSComponentExecutor();
	}
}
