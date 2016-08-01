package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.model.SHPropertyConstraint;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class SHPropertyConstraintImpl extends SHParameterizableConstraintImpl implements SHPropertyConstraint {
	
	public SHPropertyConstraintImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public Resource getClassOrDatatype() {
		Resource cls = JenaUtil.getResourceProperty(this, SH.class_);
		if(cls != null) {
			return cls;
		}
		else {
			Resource datatype = JenaUtil.getResourceProperty(this, SH.datatype);
			if(datatype != null) {
				return datatype;
			}
			else {
				Resource kind = JenaUtil.getResourceProperty(this, SH.nodeKind);
				if(SH.IRI.equals(kind) || SH.BlankNode.equals(kind)) {
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
		return SH.PropertyConstraint.inModel(getModel());
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
	public Integer getOrder() {
		return JenaUtil.getIntegerProperty(this, SH.order);
	}


	@Override
	public Resource getPath() {
		return JenaUtil.getResourceProperty(this, SH.path);
	}


	@Override
	public Property getPredicate() {
		Resource r = JenaUtil.getResourceProperty(this, SH.predicate);
		if(r != null && r.isURIResource()) {
			return new PropertyImpl(r.asNode(), (EnhGraph)r.getModel());
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