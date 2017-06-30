package org.topbraid.spin.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.topbraid.shacl.model.SHNodeShape;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.AbstractFunction2;
import org.topbraid.spin.vocabulary.SPIN;
import org.topbraid.spin.vocabulary.SPL;

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
		String key = OntologyOptimizations.get().getKeyIfEnabledFor(graph);
		if(key != null) {
			ClassMetadata classMetadata = OntologyOptimizations.get().getClassMetadata(cls, graph, key);
			Node localRange = classMetadata.getPropertyLocalRange(property, graph);
			if(localRange != null) {
				return localRange;
			}
			else {
				Node global = getGlobalRange(property, graph);
				if(global == null && useDefault) {
					return getDefaultRange(property, graph);
				}
				else {
					return global;
				}
			}
		}
		else {
			Node result = walk(property, cls, graph, new HashSet<Node>());
			if(result == null) {
				result = getGlobalRange(property, graph);
				if(result == null && useDefault) {
					result = getDefaultRange(property, graph);
				}
			}
			return result;
		}
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
		Node range = JenaNodeUtil.getObject(property, RDFS.range.asNode(), graph);
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
		
		if(SHACLUtil.exists(graph)) {
			
			for(SHNodeShape shape : SHACLUtil.getDirectShapesAtClassOrShape((Resource)ModelFactory.createModelForGraph(graph).asRDFNode(type))) {
				
				Node paramResult = walkPropertyShape(graph, shape.asNode(), property, SH.parameter.asNode());
				if(paramResult != null) {
					return paramResult;
				}
				
				Node propertyResult = walkPropertyShape(graph, shape.asNode(), property, SH.property.asNode());
				if(propertyResult != null) {
					return propertyResult;
				}
			}
		}		

		List<Node> superClasses = new LinkedList<Node>();
		{
			ExtendedIterator<Triple> it = graph.find(type, RDFS.subClassOf.asNode(), Node.ANY);
			while(it.hasNext()) {
				Node superClass = it.next().getObject();
				if(superClass.isBlank() &&
						graph.contains(superClass, OWL.onProperty.asNode(), property)) {
					Node allValuesFrom = JenaNodeUtil.getObject(superClass, OWL.allValuesFrom.asNode(), graph);
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
						(graph.contains(constraint, RDF.type.asNode(), SPL.Argument.asNode()) ||
						 graph.contains(constraint, RDF.type.asNode(), SPL.Attribute.asNode()))) {
					Node valueType = JenaNodeUtil.getObject(constraint, SPL.valueType.asNode(), graph);
					if(valueType != null) {
						it.close();
						return valueType;
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
	
	
	private static Node walkPropertyShape(Graph graph, Node shape, Node predicate, Node systemPredicate) {
		ExtendedIterator<Triple> it = graph.find(shape, systemPredicate, Node.ANY);
		while(it.hasNext()) {
			Node propertyShape = it.next().getObject();
			if(!propertyShape.isLiteral()) {
				if(graph.contains(propertyShape, SH.path.asNode(), predicate)) {
					if(!graph.contains(propertyShape, SH.deactivated.asNode(), JenaDatatypes.TRUE.asNode())) {
						Node valueType = walkPropertyShapesHelper(propertyShape, graph);
						if(valueType != null) {
							it.close();
							return valueType;
						}
					}
				}
			}
		}
		return null;
	}


	public static Node walkPropertyShapesHelper(Node propertyShape, Graph graph) {
		Node valueType = JenaNodeUtil.getObject(propertyShape, SH.class_.asNode(), graph);
		if(valueType != null) {
			return valueType;
		}
		Node datatype = JenaNodeUtil.getObject(propertyShape, SH.datatype.asNode(), graph);
		if(datatype != null) {
			return datatype;
		}
		ExtendedIterator<Triple> ors = graph.find(propertyShape, SH.or.asNode(), Node.ANY);
		while(ors.hasNext()) {
			Node or = ors.next().getObject();
			Node first = JenaNodeUtil.getObject(or, RDF.first.asNode(), graph);
			if(!first.isLiteral()) {
				Node cls = JenaNodeUtil.getObject(first, SH.class_.asNode(), graph);
				if(cls != null) {
					ors.close();
					return cls;
				}
				datatype = JenaNodeUtil.getObject(first, SH.datatype.asNode(), graph);
				if(datatype != null) {
					ors.close();
					return datatype;
				}
			}
		}
		if(graph.contains(propertyShape, SH.node.asNode(), DASH.ListShape.asNode())) {
			return RDF.List.asNode();
		}
		return null;
	}
}
