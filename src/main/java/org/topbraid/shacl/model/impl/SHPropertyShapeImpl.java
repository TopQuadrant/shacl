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
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.model.SHPropertyShape;
import org.topbraid.shacl.vocabulary.SH;

public class SHPropertyShapeImpl extends SHShapeImpl implements SHPropertyShape {
	
	public SHPropertyShapeImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public Resource getClassOrDatatype() {
		Resource cls = getPropertyResourceValue(SH.class_);
		if(cls != null) {
			return cls;
		}
		else {
			Resource datatype = getPropertyResourceValue(SH.datatype);
			if(datatype != null) {
				return datatype;
			}
			else {
				Resource kind = getPropertyResourceValue(SH.nodeKind);
				if(SH.IRI.equals(kind) || SH.BlankNode.equals(kind) || SH.BlankNodeOrIRI.equals(kind)) {
					return RDFS.Resource.inModel(getModel());
				}
				else if(SH.Literal.equals(kind)) {
					return RDFS.Literal.inModel(getModel());
				}
				else {
					return null;
				}
			}
		}
	}
	
	
	@Override
	public Resource getContext() {
		return SH.PropertyShape.inModel(getModel());
	}


	@Override
	public String getCountDisplayString() {
		Integer minCount = getMinCount();
		Integer maxCount = getMaxCount();
		return "[" + (minCount == null ? 0 : minCount) + ".." + (maxCount == null ? "*" : maxCount) + "]";
	}


	@Override
	public String getDescription() {
		return JenaUtil.getStringProperty(this, SH.description);
	}


	@Override
	public Integer getMaxCount() {
		return JenaUtil.getIntegerProperty(this, SH.maxCount);
	}


	@Override
	public Integer getMinCount() {
		return JenaUtil.getIntegerProperty(this, SH.minCount);
	}


	@Override
	public String getName() {
		return JenaUtil.getStringProperty(this, SH.name);
	}

	
	@Override
	public Integer getOrder() {
		return JenaUtil.getIntegerProperty(this, SH.order);
	}


	@Override
	public Property getPredicate() {
		Resource r = getPropertyResourceValue(SH.path);
		if(r != null && r.isURIResource()) {
			return JenaUtil.asProperty(r);
		}
		else {
			return null;
		}
	}


	@Override
	public String getVarName() {
		Property argProperty = getPredicate();
		if(argProperty != null) {
			return argProperty.getLocalName();
		}
		else {
			return null;
		}
	}


	@Override
    public String toString() {
		return "Property " + getVarName();
	}
}