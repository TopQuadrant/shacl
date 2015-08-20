package org.topbraid.shacl.constraints;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.topbraid.shacl.model.SHACLConstraint;
import org.topbraid.shacl.model.SHACLRule;
import org.topbraid.shacl.model.SHACLTemplateCall;
import org.topbraid.shacl.rules.RuleExecutable;
import org.topbraid.shacl.vocabulary.SH;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * An ExecutionLanguage that serves as fall-back, if a constraint or template does not define
 * any valid executable property such as sh:sparql.
 * This will simply report a fatal error that the constraint could not be evaluated.
 * 
 * @author Holger Knublauch
 */
public class FallbackExecutionLanguage implements ExecutionLanguage {

	
	@Override
	public boolean canExecuteConstraint(ConstraintExecutable executable) {
		return true;
	}
	
	@Override
	public boolean canExecuteRule(RuleExecutable executable) {
		return true;
	}

	
	@Override
	public boolean canExecuteScope(Resource executable) {
		return true;
	}


	@Override
	public void executeConstraint(Dataset dataset, Resource shape,
			URI shapesGraphURI, SHACLConstraint constraint, ConstraintExecutable executable,
			Resource focusNode, Model results) {
		Resource vio = results.createResource(SH.FatalError);
		vio.addProperty(SH.message, "No execution language found for constraint");
		vio.addProperty(SH.source, constraint);
		if(focusNode != null) {
			vio.addProperty(SH.root, focusNode);
		}
	}
	
	@Override
	public void executeRule(Dataset dataset, Resource shape,
			URI shapesGraphURI, SHACLRule rule, RuleExecutable executable,
			Resource focusNode, Model results,
			Map<Resource,List<SHACLConstraint>> map) {
		Resource vio = results.createResource(SH.FatalError);
		vio.addProperty(SH.message, "No execution language found for rule");
		vio.addProperty(SH.source, rule);
		if(focusNode != null) {
			vio.addProperty(SH.root, focusNode);
		}
	}


	@Override
	public Iterable<Resource> executeScope(Dataset dataset,
			Resource executable, SHACLTemplateCall templateCall) {
		return Collections.emptyList();
	}


	@Override
	public boolean isNodeInScope(Resource focusNode, Dataset dataset,
			Resource executable, SHACLTemplateCall templateCall) {
		return false;
	}
}
