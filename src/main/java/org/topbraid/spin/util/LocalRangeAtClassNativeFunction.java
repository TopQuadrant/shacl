package org.topbraid.spin.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.AbstractFunction2;
import org.topbraid.spin.vocabulary.SPIN;
import org.topbraid.spin.vocabulary.SPL;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * A native implementation of swa:localRangeAtClass, to optimize performance.
 * 
 * The original spin:body is
 * 
 * 		SELECT (COALESCE(
 * 				spif:walkObjects(?class, rdfs:subClassOf, swa:allValuesFromFunctor, ?property), 
 * 				spif:walkObjects(?class, rdfs:subClassOf, swa:splValueTypeFunctor, ?property), 
 * 				swa:globalRange(?property), 
 * 				swa:defaultRange(?property)) AS ?result)
 * 		WHERE {
 *      }
 * 
 * @author Holger Knublauch
 */
public class LocalRangeAtClassNativeFunction extends AbstractFunction2 {
	
	@Override
	protected NodeValue exec(Node cls, Node property, FunctionEnv env) {
		Graph graph = env.getActiveGraph();
		Node result = run(cls, property, graph, true);
		return NodeValue.makeNode(result);
	}

	
	private static Set<Node> datatypeOrAnnotationProperty = new HashSet<Node>();
	
	static {
		datatypeOrAnnotationProperty.add(OWL.DatatypeProperty.asNode());
		datatypeOrAnnotationProperty.add(OWL.AnnotationProperty.asNode());
	}


	public static Node run(Node cls, Node property, Graph graph, boolean useDefault) {
		Node result = walk(property, cls, graph, new HashSet<Node>());
		if(result == null) {
			result = getGlobalRange(property, graph);
			if(result == null && useDefault) {
				result = getDefaultRange(property, graph);
			}
		}
		return result;
	}
	
	
	private static Node getDefaultRange(Node property, Graph graph) {
		if(instanceOf(property, datatypeOrAnnotationProperty, graph)) {
			return XSD.xstring.asNode();
		}
		else {
			return RDFS.Resource.asNode();
		}
	}
	
	
	private static Node getGlobalRange(Node property, Graph graph) {
		return getGlobalRangeHelper(property, graph, new HashSet<Node>());
	}
	
	
	private static Node getGlobalRangeHelper(Node property, Graph graph, Set<Node> reached) {
		reached.add(property);
		Node range = getObject(property, RDFS.range.asNode(), graph);
		if(range != null) {
			return range;
		}
		for(Triple t : graph.find(property, RDFS.subPropertyOf.asNode(), Node.ANY).toList()) {
			Node superProperty = t.getObject();
			if(!reached.contains(superProperty)) {
				Node global = getGlobalRangeHelper(superProperty, graph, reached);
				if(global != null) {
					return global;
				}
			}
		}
		return null;
	}
	
	
	private static Node getObject(Node subject, Node predicate, Graph graph) {
		ExtendedIterator<Triple> it = graph.find(subject, predicate, Node.ANY);
		if(it.hasNext()) {
			Node object = it.next().getObject();
			it.close();
			return object;
		}
		return null;
	}
	
	
	private static boolean instanceOf(Node instance, Collection<Node> matchTypes, Graph graph) {
		Set<Node> reachedTypes = new HashSet<Node>();
		for(Triple t : graph.find(instance, RDF.type.asNode(), Node.ANY).toList()) {
			if(instanceOfHelper(matchTypes, t.getObject(), graph, reachedTypes)) {
				return true;
			}
		}
		return false;
	}
	
	
	private static boolean instanceOfHelper(Collection<Node> matchTypes, Node type, Graph graph, Set<Node> reachedTypes) {
		if(reachedTypes.contains(type)) {
			return false;
		}
		
		if(matchTypes.contains(type)) {
			return true;
		}
		
		reachedTypes.add(type);
		
		ExtendedIterator<Triple> it = graph.find(type, RDFS.subClassOf.asNode(), Node.ANY);
		while(it.hasNext()) {
			Node superClass = it.next().getObject();
			if(instanceOfHelper(matchTypes, superClass, graph, reachedTypes)) {
				it.close();
				return true;
			}
		}
		
		return false;
	}	

	
	private static Node walk(Node property, Node type, Graph graph, Set<Node> classes) {
		
		classes.add(type);

		List<Node> superClasses = new LinkedList<Node>();
		{
			ExtendedIterator<Triple> it = graph.find(type, RDFS.subClassOf.asNode(), Node.ANY);
			while(it.hasNext()) {
				Node superClass = it.next().getObject();
				if(superClass.isBlank() &&
						graph.contains(superClass, OWL.onProperty.asNode(), property)) {
					Node allValuesFrom = getObject(superClass, OWL.allValuesFrom.asNode(), graph);
					if(allValuesFrom != null) {
						it.close();
						return allValuesFrom;
					}
				}
				else if(superClass.isURI()) {
					superClasses.add(superClass);
				}
			}
		}		
		
		if(!(graph instanceof OptimizedMultiUnion) || ((OptimizedMultiUnion)graph).getIncludesSPIN()) {
			ExtendedIterator<Triple> it = graph.find(type, SPIN.constraint.asNode(), Node.ANY);
			while(it.hasNext()) {
				Node constraint = it.next().getObject();
				if(graph.contains(constraint, SPL.predicate.asNode(), property) &&
						graph.contains(constraint, RDF.type.asNode(), SPL.Argument.asNode())) {
					Node valueType = getObject(constraint, SPL.valueType.asNode(), graph);
					if(valueType != null) {
						it.close();
						return valueType;
					}
				}
			}
		}		
		
		if(!(graph instanceof OptimizedMultiUnion) || ((OptimizedMultiUnion)graph).getIncludesSHACL()) {
			
			{
				ExtendedIterator<Triple> it = graph.find(type, SH.argument.asNode(), Node.ANY);
				while(it.hasNext()) {
					Node constraint = it.next().getObject();
					if(constraint.isBlank() || constraint.isURI()) {
						if(graph.contains(constraint, SH.predicate.asNode(), property)) {
							Node valueType = getObject(constraint, SH.class_.asNode(), graph);
							if(valueType != null) {
								it.close();
								return valueType;
							}
							Node datatype = getObject(constraint, SH.datatype.asNode(), graph);
							if(datatype != null) {
								it.close();
								return datatype;
							}
						}
					}
				}
			}

			{
				ExtendedIterator<Triple> it = graph.find(type, SH.property.asNode(), Node.ANY);
				while(it.hasNext()) {
					Node constraint = it.next().getObject();
					if(constraint.isBlank() || constraint.isURI()) {
						if(graph.contains(constraint, SH.predicate.asNode(), property)) {
							Node valueType = getObject(constraint, SH.class_.asNode(), graph);
							if(valueType != null) {
								it.close();
								return valueType;
							}
							Node datatype = getObject(constraint, SH.datatype.asNode(), graph);
							if(datatype != null) {
								it.close();
								return datatype;
							}
						}
					}
				}
			}
		}		
		
		for(Node superClass : superClasses) {
			if(!classes.contains(superClass)) {
				Node result = walk(property, superClass, graph, classes);
				if(result != null) {
					return result;
				}
			}
		}
		
		return null;
	}
}
