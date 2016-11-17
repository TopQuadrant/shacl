package org.topbraid.shacl.arq.functions;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.topbraid.spin.arq.AbstractFunction2;

/**
 * The function spif:isValidForDatatype
 * 
 * @author Holger Knublauch
 */
public class IsValidForDatatypeFunction extends AbstractFunction2 {

	@Override
	protected NodeValue exec(Node literalNode, Node datatypeNode, FunctionEnv env) {
		
		if(literalNode == null || !literalNode.isLiteral()) {
			throw new ExprEvalException();
		}
		String lex = literalNode.getLiteralLexicalForm();
		
		if(!datatypeNode.isURI()) {
			throw new ExprEvalException();
		}
		RDFDatatype datatype = TypeMapper.getInstance().getTypeByName(datatypeNode.getURI());
		
		if(datatype == null) {
			return NodeValue.TRUE;
		}
		else {
			boolean valid = datatype.isValid(lex);
			return NodeValue.makeBoolean(valid);
		}
	}
}
