package org.topbraid.shacl.validation.java;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.XSD;
import org.topbraid.jenax.util.DiffGraph;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;

/**
 * Validator for sh:datatype constraints.
 * 
 * @author Holger Knublauch
 */
public class DatatypeConstraintExecutor extends AbstractNativeConstraintExecutor {

	// A Function that tests whether a given Model is stored in Apache Jena TDB1.
    // TDB1 does not preserve datatypes.
	// This is overloaded by TopBraid which has logic to wrap graphs
    // with additional characteristics.
	public static Predicate<Graph> isStoredInTDB1 = (graph -> false);

	// A map of XSD datatypes to the canonical type that is used inside of TDB 1 when the value is queried back
	// See also NodeId.datatypes
	private static Map<RDFDatatype,RDFDatatype> tdbTypes = new HashMap<>();
	static {
	    // Derived types of xsd:integer
		tdbTypes.put(XSDDatatype.XSDbyte, XSDDatatype.XSDinteger);
		tdbTypes.put(XSDDatatype.XSDint, XSDDatatype.XSDinteger);
		tdbTypes.put(XSDDatatype.XSDlong, XSDDatatype.XSDinteger);
		tdbTypes.put(XSDDatatype.XSDnegativeInteger, XSDDatatype.XSDinteger);
		tdbTypes.put(XSDDatatype.XSDnonNegativeInteger, XSDDatatype.XSDinteger);
		tdbTypes.put(XSDDatatype.XSDnonPositiveInteger, XSDDatatype.XSDinteger);
		tdbTypes.put(XSDDatatype.XSDpositiveInteger, XSDDatatype.XSDinteger);
		tdbTypes.put(XSDDatatype.XSDshort, XSDDatatype.XSDinteger);
		tdbTypes.put(XSDDatatype.XSDunsignedByte, XSDDatatype.XSDinteger);
		tdbTypes.put(XSDDatatype.XSDunsignedInt, XSDDatatype.XSDinteger);
		tdbTypes.put(XSDDatatype.XSDunsignedLong, XSDDatatype.XSDinteger);
		tdbTypes.put(XSDDatatype.XSDunsignedShort, XSDDatatype.XSDinteger);
        // Derived types of xsd:dateTime
		tdbTypes.put(XSDDatatype.XSDdateTimeStamp, XSDDatatype.XSDdateTime);
	}


	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		long startTime = System.currentTimeMillis();
		RDFNode datatypeNode = constraint.getParameterValue();
		String datatypeURI = datatypeNode.asNode().getURI();
		RDFDatatype datatype = NodeFactory.getType(datatypeURI);
		String message = "Value must be a valid literal of type " + ((Resource)datatypeNode).getLocalName() + (datatypeNode.equals(XSD.date) ? " e.g. ('YYYY-MM-DD')" : "");

		long valueNodeCount = 0;
		for(RDFNode focusNode : focusNodes) {
			for(RDFNode valueNode : engine.getValueNodes(constraint, focusNode)) {
				valueNodeCount++;
				validate(constraint, engine, message, datatypeURI, datatype, focusNode, valueNode);
			}
			engine.checkCanceled();
		}
		addStatistics(engine, constraint, startTime, focusNodes.size(), valueNodeCount);
	}


	private static boolean isTDB1(Graph graph) {
		if(graph instanceof MultiUnion) {
			for(Graph subGraph : JenaUtil.getSubGraphs((MultiUnion)graph)) {
				if(isTDB1(subGraph)) {
					return true;
				}
			}
			return false;
		}
		else if(graph instanceof DiffGraph) {
			return isTDB1(((DiffGraph)graph).getDelegate());
		}
		else {
			return isStoredInTDB1.test(graph);
		}
	}


	private void validate(Constraint constraint, ValidationEngine engine, String message, String datatypeURI, RDFDatatype datatype, RDFNode focusNode, RDFNode valueNode) {
		if(!valueNode.isLiteral() || !datatypeURI.equals(valueNode.asNode().getLiteralDatatypeURI()) || !datatype.isValid(valueNode.asNode().getLiteralLexicalForm())) {

			// TBS-1629: Ignore violations of mapped datatypes, e.g. actual literal is xsd:integer and sh:datatype is xsd:nonNegativeInteger
			if(valueNode.isLiteral() && !datatypeURI.equals(valueNode.asNode().getLiteralDatatypeURI()) && datatype.isValid(valueNode.asNode().getLiteralLexicalForm())) {
				if(isTDB1(focusNode.getModel().getGraph())) {
					RDFDatatype tdbType = tdbTypes.get(datatype);
					if(valueNode.asNode().getLiteralDatatype().equals(tdbType)) {
						return; // Do nothing
					}
				}
			}
			
			engine.createValidationResult(constraint, focusNode, valueNode, () -> message);
		}
	}
}
