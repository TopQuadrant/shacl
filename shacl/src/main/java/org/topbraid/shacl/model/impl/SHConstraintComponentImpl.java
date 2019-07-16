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
package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.model.SHConstraintComponent;
import org.topbraid.shacl.vocabulary.SH;

public class SHConstraintComponentImpl extends SHParameterizableImpl implements SHConstraintComponent {
	
	public SHConstraintComponentImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public Resource getValidator(Resource executableType, Resource context) {
		
		Property predicate = SH.PropertyShape.equals(context) ? SH.propertyValidator : SH.nodeValidator;
		Resource validator = JenaUtil.getResourcePropertyWithType(this, predicate, executableType);
		if(validator == null) {
			validator = JenaUtil.getResourcePropertyWithType(this, SH.validator, executableType);
		}
		
		return validator;
	}


	@Override
	public boolean isCore() {
		// Not entirely correct - someone may define their own component in the SH namespace, but close enough
		return SH.NS.equals(getNameSpace()) &&
				!SH.JSConstraintComponent.equals(this) &&
				!SH.SPARQLConstraintComponent.equals(this);
	}
}
