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
package org.topbraid.shacl.expr;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.expr.lib.AskExpression;
import org.topbraid.shacl.expr.lib.ConstantTermExpression;
import org.topbraid.shacl.expr.lib.CountExpression;
import org.topbraid.shacl.expr.lib.DistinctExpression;
import org.topbraid.shacl.expr.lib.ExistsExpression;
import org.topbraid.shacl.expr.lib.FilterShapeExpression;
import org.topbraid.shacl.expr.lib.FocusNodeExpression;
import org.topbraid.shacl.expr.lib.FunctionExpression;
import org.topbraid.shacl.expr.lib.GroupConcatExpression;
import org.topbraid.shacl.expr.lib.IfExpression;
import org.topbraid.shacl.expr.lib.IntersectionExpression;
import org.topbraid.shacl.expr.lib.LimitExpression;
import org.topbraid.shacl.expr.lib.MaxExpression;
import org.topbraid.shacl.expr.lib.MinExpression;
import org.topbraid.shacl.expr.lib.MinusExpression;
import org.topbraid.shacl.expr.lib.OffsetExpression;
import org.topbraid.shacl.expr.lib.OrderByExpression;
import org.topbraid.shacl.expr.lib.PathExpression;
import org.topbraid.shacl.expr.lib.SelectExpression;
import org.topbraid.shacl.expr.lib.SumExpression;
import org.topbraid.shacl.expr.lib.UnionExpression;
import org.topbraid.shacl.validation.sparql.SPARQLSubstitutions;
import org.topbraid.shacl.vocabulary.SH;

public class NodeExpressionFactory {
	
	private static Map<Property,BiFunction<Resource,RDFNode,NodeExpression>> constructors = new HashMap<>();
	
	static {
		
		constructors.put(SH.ask, (resource, ask) -> {
			String queryString = ask.asNode().getLiteralLexicalForm();
			Query arqQuery = ARQFactory.get().createQuery(SPARQLSubstitutions.withPrefixes(queryString, resource));
			Statement nodesS = resource.getProperty(SH.nodes);
			NodeExpression input = nodesS != null ? get().create(nodesS.getObject()) : null;
			return new AskExpression(resource, arqQuery, input, queryString);
		});
		
		constructors.put(SH.count, (resource, count) -> {
			NodeExpression nodes = get().create(count);
			return new CountExpression(resource, nodes);
		});
		
		constructors.put(SH.distinct, (resource, distinct) -> {
			NodeExpression nodes = get().create(distinct);
			return new DistinctExpression(resource, nodes);
		});
		
		constructors.put(SH.exists, (resource, exists) -> {
			NodeExpression nodes = get().create(exists);
			return new ExistsExpression(resource, nodes);
		});
		
		constructors.put(SH.filterShape, (resource, filterShape) -> {
			if(filterShape instanceof Resource) {
				NodeExpression nodes = null;
				Statement nodesS = resource.getProperty(SH.nodes);
				if(nodesS != null) {
					nodes = get().create(nodesS.getObject());
				}
				return new FilterShapeExpression(resource, nodes, (Resource) filterShape);
			}
			else {
				return null;
			}
		});
		
		constructors.put(SH.groupConcat, (resource, groupConcat) -> {
			NodeExpression nodes = get().create(groupConcat);
			return new GroupConcatExpression(resource, nodes, JenaUtil.getStringProperty(resource, SH.separator));
		});
		
		constructors.put(SH.if_, (resource, if_) -> {
			NodeExpression nodes = get().create(if_);
			Statement thenS = resource.getProperty(SH.then); 
			NodeExpression then = thenS != null ? get().create(thenS.getObject()) : null;
			Statement elseS = resource.getProperty(SH.else_); 
			NodeExpression else_ = elseS != null ? get().create(elseS.getObject()) : null;
			return new IfExpression(resource, nodes, then, else_);
		});
		
		constructors.put(SH.intersection, (resource, intersection) -> {
			List<NodeExpression> inputs = new LinkedList<>();
			RDFList list = intersection.as(RDFList.class);
			for(RDFNode member : list.iterator().toList()) {
				inputs.add(get().create(member));
			}
			return new IntersectionExpression(resource, inputs);
		});
		
		constructors.put(SH.limit, (resource, limit) -> {
			Statement nodesS = resource.getProperty(SH.nodes);
			if(nodesS != null && limit.isLiteral()) {
				NodeExpression nodes = get().create(nodesS.getObject());
				int l = limit.asLiteral().getInt();
				return new LimitExpression(resource, nodes, l);
			}
			else {
				return null;
			}
		});
		
		constructors.put(SH.max, (resource, max) -> {
			NodeExpression nodes = get().create(max);
			return new MaxExpression(resource, nodes);
		});
		
		constructors.put(SH.min, (resource, min) -> {
			NodeExpression nodes = get().create(min);
			return new MinExpression(resource, nodes);
		});
		
		constructors.put(SH.minus, (resource, minus) -> {
			Statement nodesS = resource.getProperty(SH.nodes);
			if(nodesS != null) {
				NodeExpression nodes = get().create(nodesS.getObject());
				NodeExpression minusExpr = get().create(minus);
				return new MinusExpression(resource, nodes, minusExpr);
			}
			else {
				return null;
			}
		});
		
		constructors.put(SH.offset, (resource, offset) -> {
			Statement nodesS = resource.getProperty(SH.nodes);
			if(nodesS != null && offset.isLiteral()) {
				NodeExpression nodes = get().create(nodesS.getObject());
				int o = offset.asLiteral().getInt();
				return new OffsetExpression(resource, nodes, o);
			}
			else {
				return null;
			}
		});
		
		constructors.put(SH.orderBy, (resource, orderBy) -> {
			Statement nodesS = resource.getProperty(SH.nodes);
			if(nodesS != null) {
				NodeExpression nodes = get().create(nodesS.getObject());
				NodeExpression comparator = get().create(orderBy);
				boolean desc = resource.hasProperty(SH.desc, JenaDatatypes.TRUE);
				return new OrderByExpression(resource, nodes, comparator, desc);
			}
			else {
				return null;
			}
		});
		
		constructors.put(SH.path, (resource, path) -> {
			NodeExpression nodes;
			Statement nodesS = resource.getProperty(SH.nodes);
			if(nodesS != null) {
				nodes = get().create(nodesS.getObject());
			}
			else {
				nodes = null;
			}
			return new PathExpression(resource, (Resource) path, nodes);
		});
		
		constructors.put(SH.select, (resource, select) -> {
			String queryString = select.asNode().getLiteralLexicalForm();
			Query arqQuery = ARQFactory.get().createQuery(SPARQLSubstitutions.withPrefixes(queryString, resource));
			Statement nodesS = resource.getProperty(SH.nodes);
			NodeExpression input = nodesS != null ? get().create(nodesS.getObject()) : null;
			return new SelectExpression(resource, arqQuery, input, queryString);
		});
		
		constructors.put(SH.sum, (resource, sum) -> {
			NodeExpression nodes = get().create(sum);
			return new SumExpression(resource, nodes);
		});
		
		constructors.put(SH.union, (resource, union) -> {
			List<NodeExpression> inputs = new LinkedList<>();
			RDFList list = union.as(RDFList.class);
			for(RDFNode member : list.iterator().toList()) {
				inputs.add(get().create(member));
			}
			return new UnionExpression(resource, inputs);
		});
	}

	private static NodeExpressionFactory singleton = new NodeExpressionFactory();
	
	public static NodeExpressionFactory get() {
		return singleton;
	}
	
	public static void set(NodeExpressionFactory value) {
		singleton = value;
	}
	

	/**
	 * Installs a new kind of node expression as a 3rd party extension.
	 * The node expression is identified by a "key" predicate (e.g. sh:sum identifies the built-in sum node expressions).
	 * @param predicate  the key predicate
	 * @param function  a factory function that takes the node expression's blank node and the value of the key property as input
	 *                  and produces a new instance of NodeExpression
	 */
	public void addPlugin(Property predicate, BiFunction<Resource,RDFNode,NodeExpression> function) {
		constructors.put(predicate, function);
	}
	
	
	public NodeExpression create(RDFNode node) {
		if(SH.this_.equals(node)) {
			return new FocusNodeExpression(node);
		}
		else if(node.isURIResource() || node.isLiteral()) {
			return new ConstantTermExpression(node);
		}
		else {

			Resource resource = node.asResource();
			StmtIterator it = resource.listProperties();
			try {
				while(it.hasNext()) {
					Statement s = it.next();
					BiFunction<Resource,RDFNode,NodeExpression> function = constructors.get(s.getPredicate());
					if(function != null) {
						NodeExpression expr = function.apply(resource, s.getObject());
						if(expr != null) {
							return expr;
						}
					}
				}
			}
			finally {
				it.close();
			}
			
			Statement s = getFunctionStatement(resource);
			if(s != null) {
				List<NodeExpression> args = new LinkedList<>();
				RDFList list = s.getResource().as(RDFList.class);
				for(RDFNode member : list.iterator().toList()) {
					args.add(create(member));
				}
				return new FunctionExpression(resource, s.getPredicate(), args);
			}
			else {
				throw new IllegalArgumentException("Malformed SHACL node expression");
			}
		}
	}

	
	public Statement getFunctionStatement(Resource resource) {
		for(Statement sc : resource.listProperties().toList()) {
			if(RDF.nil.equals(sc.getObject()) || (sc.getObject().isAnon() && sc.getResource().hasProperty(RDF.first))) {
				return sc;
			}
		}
		return null;
	}
}
