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

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHPropertyShape;
import org.topbraid.shacl.model.SHRule;
import org.topbraid.shacl.model.SHShape;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SH;

public abstract class SHShapeImpl extends SHParameterizableInstanceImpl implements SHShape {
	
	public SHShapeImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}


	@Override
	public Resource getPath() {
		return getPropertyResourceValue(SH.path);
	}


	@Override
	public List<SHPropertyShape> getPropertyShapes() {
		List<SHPropertyShape> results = new LinkedList<>();
		for(Statement s : listProperties(SH.parameter).toList()) {
			if(s.getObject().isResource()) {
				results.add(SHFactory.asPropertyShape(s.getObject()));
			}
		}
		for(Statement s : listProperties(SH.property).toList()) {
			if(s.getObject().isResource()) {
				results.add(SHFactory.asPropertyShape(s.getObject()));
			}
		}
		return results;
	}


	@Override
	public List<SHPropertyShape> getPropertyShapes(RDFNode predicate) {
		List<SHPropertyShape> results = new LinkedList<>();
		for(Resource property : JenaUtil.getResourceProperties(this, SH.parameter)) {
			if(property.hasProperty(SH.path, predicate)) {
				results.add(SHFactory.asPropertyShape(property));
			}
		}
		for(Resource property : JenaUtil.getResourceProperties(this, SH.property)) {
			if(property.hasProperty(SH.path, predicate)) {
				results.add(SHFactory.asPropertyShape(property));
			}
		}
		return results;
	}


	@Override
	public Iterable<SHRule> getRules() {
		List<SHRule> results = new LinkedList<>();
		for(Resource r : JenaUtil.getResourceProperties(this, SH.rule)) {
			results.add(r.as(SHRule.class));
		}
		return results;
	}


	@Override
	public Resource getSeverity() {
		Resource result = getPropertyResourceValue(SH.severity);
		return result != null ? result : SH.Violation;
	}


	@Override
	public boolean hasTargetNode(RDFNode node) {
		
		// rdf:type / sh:targetClass
		if(node instanceof Resource) {
			boolean shapeClass = JenaUtil.hasIndirectType(this, RDFS.Class);
			for(Resource type : JenaUtil.getAllTypes((Resource)node)) {
				if(shapeClass && type.equals(this)) {
					return true;
				}
				if(hasProperty(SH.targetClass, type)) {
					return true;
				}
			}
		}
		
		// property targets
		if(node instanceof Resource) {
			for(Statement s : listProperties(SH.targetSubjectsOf).toList()) {
				if(((Resource)node).hasProperty(JenaUtil.asProperty(s.getResource()))) {
					return true;
				}
			}
		}
		for(Statement s : listProperties(SH.targetObjectsOf).toList()) {
			if(node.getModel().contains(null, JenaUtil.asProperty(s.getResource()), node)) {
				return true;
			}
		}
		
		if(hasProperty(SH.targetNode, node)) {
			return true;
		}
		
		// sh:target
		for(Statement s : listProperties(SH.target).toList()) {
			if(SHACLUtil.isInTarget(node, ARQFactory.get().getDataset(node.getModel()), s.getResource())) {
				return true;
			}
		}

		return false;
	}


	@Override
	public boolean isDeactivated() {
		return hasProperty(SH.deactivated, JenaDatatypes.TRUE);
	}


	@Override
	public boolean isPropertyShape() {
		return hasProperty(SH.path);
	}
}