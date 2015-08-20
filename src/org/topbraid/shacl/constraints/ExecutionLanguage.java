package org.topbraid.shacl.constraints;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.topbraid.shacl.model.SHACLConstraint;
import org.topbraid.shacl.model.SHACLRule;
import org.topbraid.shacl.model.SHACLTemplateCall;
import org.topbraid.shacl.rules.RuleExecutable;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A processor that can execute constraint and scope executables.
 * The default implementation uses SPARQL, based on sh:sparql.
 * 
 * @author Holger Knublauch
 */
public interface ExecutionLanguage {
	
	boolean canExecuteConstraint(ConstraintExecutable executable);
	
	boolean canExecuteRule(RuleExecutable rule);
	
	boolean canExecuteScope(Resource executable);
	
	void executeConstraint(Dataset dataset, Resource shape, URI shapesGraphURI, SHACLConstraint constraint, ConstraintExecutable executable, Resource focusNode, Model results);
	// TODO check results for rule
	void executeRule(Dataset dataset, Resource shape, URI shapesGraphURI, SHACLRule rule, RuleExecutable executable, Resource focusNode, Model results, Map<Resource,List<SHACLConstraint>> map);
	
	Iterable<Resource> executeScope(Dataset dataset, Resource executable, SHACLTemplateCall templateCall);
	
	boolean isNodeInScope(Resource focusNode, Dataset dataset, Resource executable, SHACLTemplateCall templateCall);
}
