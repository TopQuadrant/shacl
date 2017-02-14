package org.topbraid.shacl.validation.sparql;

import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.validation.Constraint;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class SPARQLDerivedValuesExecutor extends AbstractSPARQLExecutor {
	
	public SPARQLDerivedValuesExecutor(Constraint constraint) {
		super(constraint);
	}

	
	@Override
	protected void addBindings(Constraint constraint, QuerySolutionMap bindings) {
		// Do nothing (?)
	}

	
	@Override
	protected String getLabel(Constraint constraint) {
		Resource valuesDeriver = (Resource)constraint.getParameterValue();
		String sparql = JenaUtil.getStringProperty(valuesDeriver, SH.select);
		return "SPARQL derived values " + sparql;
	}


	@Override
	protected String getSPARQL(Constraint constraint) {
		Resource valuesDeriver = (Resource)constraint.getParameterValue();
		String sparql = JenaUtil.getStringProperty(valuesDeriver, SH.select);
		int startIndex = sparql.indexOf('{');
		int endIndex = sparql.lastIndexOf('}');
		sparql = sparql.substring(startIndex + 1, endIndex - 1);
		StringBuffer sb = new StringBuffer("SELECT ?this ?value ?message");
		sb.append("\nWHERE {\n");
		sb.append("    {\n");
		sb.append("        $this $" + SH.PATHVar.getVarName() + " ?value .\n");
		sb.append("        FILTER NOT EXISTS {\n");
		sb.append(sparql);
		sb.append("        }\n");
		sb.append("        BIND (\"Existing value is not among derived values\" AS ?message) .\n");
		sb.append("    }\n");
		sb.append("    UNION {\n");
		sb.append(sparql);
		sb.append("\n");
		sb.append("        FILTER NOT EXISTS {\n");
		sb.append("            $this $" + SH.PATHVar.getVarName() + " ?value .\n");
		sb.append("        }\n");
		sb.append("        BIND (\"Derived value is not among existing values\" AS ?message) .\n");
		sb.append("    }\n");
		sb.append("}");
		return SPARQLSubstitutions.withPrefixes(sb.toString(), valuesDeriver);
	}


	@Override
	protected Resource getSPARQLExecutable(Constraint constraint) {
		return (Resource) constraint.getParameterValue();
	}
}
