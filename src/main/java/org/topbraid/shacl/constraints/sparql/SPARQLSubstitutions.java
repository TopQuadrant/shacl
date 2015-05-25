package org.topbraid.shacl.constraints.sparql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.topbraid.shacl.constraints.ModelConstraintValidator;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.util.SPINUtil;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Collects various dodgy helper algorithms by the SPARQL execution language. 
 *
 * @author Holger Knublauch
 */
class SPARQLSubstitutions {

	// TODO: Algorithm incorrect, e.g. if { is included as a comment
	static Query insertScopeClause(Query query, int scopeCount) {
		String str = query.toString();
		Pattern pattern = Pattern.compile("(?i)WHERE\\s*\\{");
		Matcher matcher = pattern.matcher(str);
		if(matcher.find()) {
			int index = matcher.end();
			StringBuilder sb = new StringBuilder(str);
			
			StringBuffer s = new StringBuffer();
			for(int i = 0; i < scopeCount; i++) {
				s.append("{ FILTER <");
				s.append(SH.hasShape.getURI());
				s.append(">(?this, ?");
				s.append(ModelConstraintValidator.SCOPE_VAR_NAME + i);
				s.append(", ?" + SH.shapesGraphVar.getVarName() + ") }");
			}
			
			sb.insert(index, s.toString());
			return ARQFactory.get().createQuery(sb.toString());
		}
		else {
			throw new IllegalArgumentException("Cannot find first '{' in query string: " + str);
		}
	}

	
	// TODO: Algorithm incorrect, e.g. if { is included as a comment
	static Query insertThisAndScopeBindingClause(Query query, int scopeCount, Property selectorProperty) {
		String str = query.toString();
		Pattern pattern = Pattern.compile("(?i)WHERE\\s*\\{");
		Matcher matcher = pattern.matcher(str);
		if(matcher.find()) {
			int index = matcher.end();
			StringBuilder sb = new StringBuilder(str);
			
			// TODO: Maybe we can do a rdfs:subClassOf* traversal here
			
			StringBuffer s = new StringBuffer();
			s.append(" {?this <" + selectorProperty + "> ?");
			s.append(SPINUtil.TYPE_CLASS_VAR_NAME);
			for(int i = 0; i < scopeCount; i++) {
				s.append(" . FILTER <");
				s.append(SH.hasShape.getURI());
				s.append(">(?this, ?");
				s.append(ModelConstraintValidator.SCOPE_VAR_NAME + i);
				s.append(", ?" + SH.shapesGraphVar.getVarName() + ")");
			}
			s.append(" }. ");
			
			sb.insert(index, s.toString());
			return ARQFactory.get().createQuery(sb.toString());
		}
		else {
			throw new IllegalArgumentException("Cannot find first '{' in query string: " + str);
		}
	}

	
	static Literal withSubstitutions(Literal template, QuerySolution bindings) {
		StringBuffer buffer = new StringBuffer();
		String labelTemplate = template.getLexicalForm();
		for(int i = 0; i < labelTemplate.length(); i++) {
			if(i < labelTemplate.length() - 3 && labelTemplate.charAt(i) == '{' && labelTemplate.charAt(i + 1) == '?') {
				int varEnd = i + 2;
				while(varEnd < labelTemplate.length()) {
					if(labelTemplate.charAt(varEnd) == '}') {
						String varName = labelTemplate.substring(i + 2, varEnd);
						RDFNode varValue = bindings.get(varName);
						if(varValue instanceof Resource) {
							buffer.append(SPINLabels.get().getLabel((Resource)varValue));
						}
						else if(varValue instanceof Literal) {
							buffer.append(varValue.asNode().getLiteralLexicalForm());
						}
						break;
					}
					else {
						varEnd++;
					}
				}
				i = varEnd;
			}
			else {
				buffer.append(labelTemplate.charAt(i));
			}
		}
		if(template.getLanguage().isEmpty()) {
			return ResourceFactory.createTypedLiteral(buffer.toString());
		}
		else {
			return ResourceFactory.createLangLiteral(buffer.toString(), template.getLanguage());
		}
	}
}
