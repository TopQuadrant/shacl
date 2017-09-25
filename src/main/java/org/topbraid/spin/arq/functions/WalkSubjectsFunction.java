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
package org.topbraid.spin.arq.functions;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * The implementation of sh:walkSubjects.
 * 
 * @author Holger Knublauch
 */
public class WalkSubjectsFunction extends AbstractWalkFunction {

	@Override
    protected ExtendedIterator<Triple> createIterator(Graph graph, Node node, Node predicate) {
		return graph.find(null, predicate, node);
	}

	
	@Override
    protected Node getNext(Triple triple) {
		return triple.getSubject();
	}
}
