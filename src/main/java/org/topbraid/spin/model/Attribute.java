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

package org.topbraid.spin.model;

import org.apache.jena.rdf.model.RDFNode;


/**
 * Jena wrapper for spl:Attribute.
 * 
 * @author Holger Knublauch
 */
public interface Attribute extends AbstractAttribute {
	
	/**
	 * Gets the declared default value of this attribute, as defined
	 * using spl:defaultValue.  Might be null.
	 * @return the default value
	 */
	RDFNode getDefaultValue();
	
	
	/**
	 * Gets the maximum cardinality of this attribute, if specified.
	 * This is based on spl:maxCount.  Null if unspecified.
	 * @return the maximum cardinality or null if none is given
	 */
	Integer getMaxCount();

	
	/**
	 * Gets the minimum cardinality of this attribute.
	 * This is based on spl:minCount.  Default value is 0.
	 * @return the minimum cardinality
	 */
	int getMinCount();
}
