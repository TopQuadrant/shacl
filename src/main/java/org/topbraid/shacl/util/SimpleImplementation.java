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

import java.lang.reflect.Constructor;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.Node;

/**
 * An extension of the Jena polymorphism mechanism.
 * 
 * @author Holger Knublauch
 */
public class SimpleImplementation extends ImplementationByType {

	@SuppressWarnings("rawtypes")
	private Constructor constructor;


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SimpleImplementation(Node type, Class implClass) {
		super(type);
		try {
			constructor = implClass.getConstructor(Node.class, EnhGraph.class);
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
	}


	@Override
	public EnhNode wrap(Node node, EnhGraph eg) {
		try {
			return (EnhNode)constructor.newInstance(node, eg);
		}
		catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}
}
