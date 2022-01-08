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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.topbraid.jenax.statistics.ExecStatistics;
import org.topbraid.jenax.statistics.ExecStatisticsManager;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.jenax.util.RDFLabels;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHSPARQLConstraint;
import org.topbraid.shacl.validation.SHACLException;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Validator for sh:sparql constraints.
 * 
 * @author Holger Knublauch
 */
public class SPARQLConstraintExecutor extends AbstractSPARQLExecutor {
	
	private boolean deactivated;

	
	public SPARQLConstraintExecutor(Constraint constraint) {
		super(constraint);
		
		this.deactivated = ((Resource)constraint.getParameterValue()).hasProperty(SH.deactivated, JenaDatatypes.TRUE);
		
		Set<String> preBoundVars = new HashSet<>();
		preBoundVars.add(SH.thisVar.getVarName());
		preBoundVars.add(SH.shapesGraphVar.getVarName());
		preBoundVars.add(SH.currentShapeVar.getVarName());
		List<String> errors = SPARQLSyntaxChecker.checkQuery(getQuery(), preBoundVars);
		if(!errors.isEmpty()) {
			throw new IllegalArgumentException(errors.size() + " violations of SPARQL Syntax rules (Appendix A): " + errors + ". Query: " + getQuery());
		}
	}

	
	private void addStatistics(ValidationEngine engine, Constraint constraint, long startTime, int focusNodeCount, long valueNodeCount) {
		if(ExecStatisticsManager.get().isRecording()) {
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			ExecStatistics stats = new ExecStatistics(constraint.getComponent().getLocalName() + " (SPARQL constraint executor)", null, duration, startTime, constraint.getComponent().asNode());
			ExecStatisticsManager.get().add(Collections.singletonList(stats));
		}
		if(engine.getProfile() != null) {
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			engine.getProfile().record(duration, focusNodeCount, valueNodeCount, constraint);
		}
	}

	
	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		
		if(deactivated) {
			return;
		}
		
		long startTime = System.currentTimeMillis();
		super.executeConstraint(constraint, engine, focusNodes);
		addStatistics(engine, constraint, startTime, focusNodes.size(), focusNodes.size());
	}

	
	@Override
	protected void addBindings(Constraint constraint, QuerySolutionMap bindings) {
		// Do nothing
	}


	@Override
	protected String getLabel(Constraint constraint) {
		return "SPARQL Constraint " + JenaUtil.getStringProperty((Resource)constraint.getParameterValue(), SH.select);
	}


	@Override
	protected String getSPARQL(Constraint constraint) {
		SHSPARQLConstraint sc = SHFactory.asSPARQLConstraint(constraint.getParameterValue());
		String select = JenaUtil.getStringProperty(sc, SH.select);
		if(select == null) {
			String message = "Missing " + SH.PREFIX + ":" + SH.select.getLocalName() + " of " + RDFLabels.get().getLabel(sc);
			if(sc.isAnon()) {
				StmtIterator it = sc.getModel().listStatements(null, null, sc);
				if(it.hasNext()) {
					Statement s = it.next();
					it.close();
					message += " at " + RDFLabels.get().getLabel(s.getSubject());
					message += " via " + RDFLabels.get().getLabel(s.getPredicate());
				}
			}
			throw new SHACLException(message);
		}
		return SPARQLSubstitutions.withPrefixes(select, sc);
	}


	@Override
	protected Resource getSPARQLExecutable(Constraint constraint) {
		return (Resource) constraint.getParameterValue();
	}
}
