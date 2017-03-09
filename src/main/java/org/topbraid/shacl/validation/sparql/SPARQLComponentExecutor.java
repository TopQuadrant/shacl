package org.topbraid.shacl.validation.sparql;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.shacl.validation.Constraint;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class SPARQLComponentExecutor extends AbstractSPARQLExecutor {
	
	public SPARQLComponentExecutor(Constraint constraint) {
		super(constraint);
	}

	
	@Override
	protected void addBindings(Constraint constraint, QuerySolutionMap bindings) {
		constraint.addBindings(bindings);
	}

	
	@Override
	protected String getLabel(Constraint constraint) {
		return constraint.getComponent().getLocalName() + " (SPARQL constraint component executor)";
	}


	@Override
	protected String getSPARQL(Constraint constraint) {
		Resource validator = constraint.getComponent().getValidator(SH.SPARQLExecutable, constraint.getContext());
		if(JenaUtil.hasIndirectType(validator, SH.SPARQLAskValidator)) {
			return createSPARQLFromAskValidator(constraint, validator);
		}
		else if(JenaUtil.hasIndirectType(validator, SH.SPARQLSelectValidator)) {
			return SPARQLSubstitutions.withPrefixes(JenaUtil.getStringProperty(validator, SH.select), validator);
		}
		return null;
	}


	@Override
	protected Resource getSPARQLExecutable(Constraint constraint) {
		return constraint.getComponent().getValidator(SH.SPARQLExecutable, constraint.getContext());
	}


	private String createSPARQLFromAskValidator(Constraint constraint, Resource validator) {
		String valueVar = "?value";
		while(constraint.getComponent().getParametersMap().containsKey(valueVar)) {
			valueVar += "_";
		}
		StringBuffer sb = new StringBuffer();
		if(SH.NodeShape.equals(constraint.getContext())) {
			sb.append("SELECT $this ?value\nWHERE {\n");
			sb.append("    BIND ($this AS ");
			sb.append(valueVar);
			sb.append(") .\n");
		}
		else {
			// Collect other variables used in sh:messages
			Set<String> otherVarNames = new HashSet<String>();
			for(Statement messageS : validator.listProperties(SH.message).toList()) {
				SPARQLSubstitutions.addMessageVarNames(messageS.getLiteral().getLexicalForm(), otherVarNames);
			}
			otherVarNames.remove(SH.pathVar.getVarName());
			otherVarNames.remove(SH.valueVar.getVarName());
			for(String varName : otherVarNames) {
				sb.append(" ?" + varName);
			}
			
			// Create body
			sb.append("SELECT DISTINCT $this ?value\nWHERE {\n");
			sb.append("    $this $" + SH.PATHVar.getVarName() + " " + valueVar + " .\n");
		}

		String sparql = JenaUtil.getStringProperty(validator, SH.ask);
		int firstIndex = sparql.indexOf('{');
		int lastIndex = sparql.lastIndexOf('}');
		String body = "{" + sparql.substring(firstIndex + 1, lastIndex + 1);
		sb.append("    FILTER NOT EXISTS " + body + "\n}");
		
		return SPARQLSubstitutions.withPrefixes(sb.toString(), validator);
	}
}
