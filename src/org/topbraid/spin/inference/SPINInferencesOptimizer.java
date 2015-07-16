package org.topbraid.spin.inference;

import java.util.List;
import java.util.Map;

import org.topbraid.spin.util.CommandWrapper;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * An interface for objects that can pre-process a set of rules, usually to optimize
 * the performance of rule execution.
 * 
 * @author Holger Knublauch
 */
public interface SPINInferencesOptimizer {

	/**
	 * Takes a rule set and either returns the same rule set unchanged or a new
	 * one with refactored rules.
	 * @param class2Query  the rules to execute
	 * @return a new rule set or class2Query unchanged
	 */
	Map<Resource, List<CommandWrapper>> optimize(Map<Resource, List<CommandWrapper>> class2Query);
}
