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
package org.topbraid.shacl.js.model;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapStd;
import org.apache.jena.sparql.util.NodeFactoryExtra;

/**
 * A partial implementation of DataFactory from
 * https://github.com/rdfjs/representation-task-force/blob/master/interface-spec.md
 * to be used by JavaScript via Nashorn.
 * 
 * @author Holger Knublauch
 */
public class TermFactory {
	
	private PrefixMap pm = new PrefixMapStd();
	
	
	public JSBlankNode blankNode() {
		return blankNode(null);
	}
	
	
	public JSBlankNode blankNode(String value) {
		Node node = value == null ?
				NodeFactory.createBlankNode() :
				NodeFactory.createBlankNode(value);
		return new JSBlankNode(node);
	}
	
	
	public JSLiteral literal(String value, Object langOrDatatype) {
		if(langOrDatatype instanceof JSNamedNode) {
			return new JSLiteral(NodeFactory.createLiteral(value, TypeMapper.getInstance().getTypeByName(((JSNamedNode)langOrDatatype).getValue())));
		}
		else if(langOrDatatype instanceof String) {
			return new JSLiteral(NodeFactory.createLiteral(value, (String)langOrDatatype));
		}
		else {
			throw new IllegalArgumentException("Invalid type of langOrDatatype argument");
		}
	}

	
	public JSNamedNode namedNode(String value) {
		Node node = NodeFactory.createURI(value);
		return new JSNamedNode(node);
	}
	
	
	public void registerNamespace(String prefix, String namespace) {
		pm.add(prefix, namespace);
	}
	
	
	public JSTerm term(String str) {
        Node n;
        try {
        	n = NodeFactoryExtra.parseNode(str, pm);
        }
        catch(Exception ex) {
        	throw new IllegalArgumentException("Cannot parse node \"" + str + "\"", ex);
        }
        if(n.isURI()) {
        	return new JSNamedNode(n);
        }
        else if(n.isLiteral()) {
        	return new JSLiteral(n);
        }
        else if(n.isBlank()) {
        	return new JSBlankNode(n);
        }
        else {
        	throw new IllegalArgumentException("Unexpected node type for " + n);
        }
	}
}
