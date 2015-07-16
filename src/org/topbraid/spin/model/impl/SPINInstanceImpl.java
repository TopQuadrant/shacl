/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import java.util.LinkedList;
import java.util.List;

import org.topbraid.spin.model.QueryOrTemplateCall;
import org.topbraid.spin.model.SPINInstance;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.SPINUtil;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;


public class SPINInstanceImpl extends ResourceImpl implements SPINInstance {

	public SPINInstanceImpl(Node node, EnhGraph eg) {
		super(node, eg);
	}

	
	public List<QueryOrTemplateCall> getQueriesAndTemplateCalls(Property predicate) {
		List<QueryOrTemplateCall> results = new LinkedList<QueryOrTemplateCall>();
		for(Resource cls : JenaUtil.getAllTypes(this)) {
			SPINUtil.addQueryOrTemplateCalls(cls, predicate, results);
		}
		return results;
	}
}
