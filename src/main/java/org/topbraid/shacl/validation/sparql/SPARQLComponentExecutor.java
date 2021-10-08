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
package org.topbraid.shacl.validation.sparql;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.model.SHParameter;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Validator for user-defined SPARQL constraint components.
 * 
 * @author Holger Knublauch
 */
public class SPARQLComponentExecutor extends AbstractSPARQLExecutor {
	
	private boolean wasAsk;
	
	public SPARQLComponentExecutor(Constraint constraint) {
		super(constraint);
		
		if(!SH.NS.equals(constraint.getComponent().getNameSpace())) {
			Set<String> preBoundVars = new HashSet<>();
			for(SHParameter param : constraint.getComponent().getParameters()) {
				preBoundVars.add(param.getVarName());
			}
			preBoundVars.add(SH.thisVar.getVarName());
			preBoundVars.add(SH.shapesGraphVar.getVarName());
			preBoundVars.add(SH.currentShapeVar.getVarName());
			if(wasAsk) {
				preBoundVars.add(SH.valueVar.getVarName());
			}
			List<String> errors = SPARQLSyntaxChecker.checkQuery(getQuery(), preBoundVars);
			if(!errors.isEmpty()) {
				throw new IllegalArgumentException(errors.size() + 
						" violations of SPARQL Syntax rules (Appendix A): " + errors + ". Query: " + getQuery());
			}
		}
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
		
		this.wasAsk = true;
		
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
			sb.append("SELECT DISTINCT $this ?value");

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
			
			sb.append("\nWHERE {\n");
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
