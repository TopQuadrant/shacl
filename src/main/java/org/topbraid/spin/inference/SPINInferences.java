/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.inference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Command;
import org.topbraid.spin.progress.ProgressMonitor;
import org.topbraid.spin.statistics.SPINStatistics;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.util.CommandWrapper;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.QueryWrapper;
import org.topbraid.spin.util.SPINQueryFinder;
import org.topbraid.spin.util.SPINUtil;
import org.topbraid.spin.util.UpdateUtil;
import org.topbraid.spin.util.UpdateWrapper;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.update.Update;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.vocabulary.RDF;


/**
 * A service to execute inference rules based on the spin:rule property.
 * 
 * @author Holger Knublauch
 */
public class SPINInferences { 
	
	/**
	 * The globally registered optimizers
	 */
	private static List<SPINInferencesOptimizer> optimizers = new LinkedList<SPINInferencesOptimizer>();
	
	public static void addOptimizer(SPINInferencesOptimizer optimizer) {
		optimizers.add(optimizer);
	}
	
	public static void removeOptimizer(SPINInferencesOptimizer optimizer) {
		optimizers.remove(optimizer);
	}
	
	
	/**
	 * Checks if a given property is a SPIN rule property.
	 * This is (currently) defined as a property that has type spin:RuleProperty
	 * or is a sub-property of spin:rule.  The latter condition may be removed
	 * at some later stage after people have upgraded to SPIN 1.1 conventions.
	 * @param property  the property to check
	 * @return true if property is a rule property
	 */
	public static boolean isRuleProperty(Property property) {
		if(SPIN.rule.equals(property)) {
			return true;
		}
		else if(JenaUtil.hasSuperProperty(property, property.getModel().getProperty(SPIN.rule.getURI()))) {
			return true;
		}
		else {
			return JenaUtil.hasIndirectType(property, SPIN.RuleProperty.inModel(property.getModel())); 
		}
	}
	
	
	/**
	 * See the other run method for help - this is using spin:rule as rulePredicate.
	 * @param queryModel  the Model to query
	 * @param newTriples  the Model to add the new triples to 
	 * @param explanations  an optional object to write explanations to
	 * @param statistics  optional list to add statistics about which queries were slow
	 * @param singlePass  true to just do a single pass (don't iterate)
	 * @param monitor  an optional ProgressMonitor
	 * @return the number of iterations (1 with singlePass)
	 * @see #run(Model, Property, Model, SPINExplanations, List, boolean, ProgressMonitor)
	 */
	public static int run(
			Model queryModel, 
			Model newTriples,
			SPINExplanations explanations,
			List<SPINStatistics> statistics,
			boolean singlePass, 
			ProgressMonitor monitor) {
		return run(queryModel, SPIN.rule, newTriples, explanations, statistics, singlePass, monitor);
	}
	
	
	/**
	 * Iterates over all SPIN rules in a (query) Model and adds all constructed
	 * triples to a given Model (newTriples) until no further changes have been
	 * made within one iteration.
	 * Note that in order to iterate more than single pass, the newTriples Model
	 * must be a sub-model of the queryModel (which likely has to be an OntModel).
	 * The supplied rulePredicate is usually spin:rule, but can also be a sub-
	 * property of spin:rule to exercise finer control over which rules to fire.
	 * @param queryModel  the Model to query
	 * @param rulePredicate  the rule predicate (spin:rule or a sub-property thereof)
	 * @param newTriples  the Model to add the new triples to 
	 * @param explanations  an optional object to write explanations to
	 * @param statistics  optional list to add statistics about which queries were slow
	 * @param singlePass  true to just do a single pass (don't iterate)
	 * @param monitor  an optional ProgressMonitor
	 * @return the number of iterations (1 with singlePass)
	 */
	public static int run(
			Model queryModel,
			Property rulePredicate,
			Model newTriples,
			SPINExplanations explanations,
			List<SPINStatistics> statistics,
			boolean singlePass, 
			ProgressMonitor monitor) {
		Map<Resource,List<CommandWrapper>> cls2Query = SPINQueryFinder.getClass2QueryMap(queryModel, queryModel, rulePredicate, true, false);
		Map<Resource,List<CommandWrapper>> cls2Constructor = SPINQueryFinder.getClass2QueryMap(queryModel, queryModel, SPIN.constructor, true, false);
		SPINRuleComparator comparator = new DefaultSPINRuleComparator(queryModel);
		return run(queryModel, newTriples, cls2Query, cls2Constructor, explanations, statistics, singlePass, rulePredicate, comparator, monitor);
	}

	
	/**
	 * Iterates over a provided collection of SPIN rules and adds all constructed
	 * triples to a given Model (newTriples) until no further changes have been
	 * made within one iteration.
	 * Note that in order to iterate more than single pass, the newTriples Model
	 * must be a sub-model of the queryModel (which likely has to be an OntModel).
	 * @param queryModel  the Model to query
	 * @param newTriples  the Model to add the new triples to 
	 * @param class2Query  the map of queries to run (see SPINQueryFinder)
	 * @param class2Constructor  the map of constructors to run
	 * @param explanations  an optional object to write explanations to
	 * @param statistics  optional list to add statistics about which queries were slow
	 * @param singlePass  true to just do a single pass (don't iterate)
	 * @param rulePredicate  the predicate used (e.g. spin:rule)
	 * @param comparator  optional comparator to determine the order of rule execution
	 * @param monitor  an optional ProgressMonitor
	 * @return the number of iterations (1 with singlePass)
	 */
	public static int run(
			Model queryModel,
			Model newTriples,
			Map<Resource, List<CommandWrapper>> class2Query,
			Map<Resource, List<CommandWrapper>> class2Constructor,
			SPINExplanations explanations,
			List<SPINStatistics> statistics,
			boolean singlePass,
			Property rulePredicate,
			SPINRuleComparator comparator,
			ProgressMonitor monitor) {
		
		// Run optimizers (if available)
		for(SPINInferencesOptimizer optimizer : optimizers) {
			class2Query = optimizer.optimize(class2Query);
		}
		
		// Get sorted list of Rules and remember where they came from
		List<CommandWrapper> rulesList = new ArrayList<CommandWrapper>();
		Map<CommandWrapper,Resource> rule2Class = new HashMap<CommandWrapper,Resource>();
		for(Resource cls : class2Query.keySet()) {
			List<CommandWrapper> queryWrappers = class2Query.get(cls);
			for(CommandWrapper queryWrapper : queryWrappers) {
				rulesList.add(queryWrapper);
				rule2Class.put(queryWrapper, cls);
			}
		}
		if(comparator != null) {
			Collections.sort(rulesList, comparator);
		}
		
		// Make sure the rulePredicate has a Model attached to it
		if(rulePredicate.getModel() == null) {
			rulePredicate = queryModel.getProperty(rulePredicate.getURI());
		}
		
		// Iterate
		int iteration = 1;
		boolean changed;
		do {
			Set<Statement> newRules = new HashSet<Statement>();
			changed = false;
			for(CommandWrapper arqWrapper : rulesList) {
				
				// Skip rule if needed
				if(arqWrapper.getStatement() != null) {
					Property predicate = arqWrapper.getStatement().getPredicate();
					Integer maxIterationCount = JenaUtil.getIntegerProperty(predicate, SPIN.rulePropertyMaxIterationCount);
					if(maxIterationCount != null) {
						if(iteration > maxIterationCount) {
							continue;
						}
					}
				}
				
				Resource cls = rule2Class.get(arqWrapper);
					
				if(monitor != null) {
					
					if(monitor.isCanceled()) {
						return iteration - 1;
					}
					
					StringBuffer sb = new StringBuffer("TopSPIN iteration ");
					sb.append(iteration);
					sb.append(" at ");
					sb.append(SPINLabels.get().getLabel(cls));
					sb.append(", rule ");
					sb.append(arqWrapper.getLabel() != null ? arqWrapper.getLabel() : arqWrapper.getText());
					monitor.subTask(sb.toString());
				}

				StringBuffer sb = new StringBuffer();
				sb.append("Inferred by ");
				sb.append(SPINLabels.get().getLabel(rulePredicate));
				sb.append(" at class ");
				sb.append(SPINLabels.get().getLabel(cls));
				sb.append(":\n\n" + arqWrapper.getText());
				String explanationText = sb.toString();
				boolean thisUnbound = arqWrapper.isThisUnbound();
				changed |= runCommandOnClass(arqWrapper, arqWrapper.getLabel(), queryModel, newTriples, cls, true, class2Constructor, statistics, explanations, explanationText, newRules, thisUnbound, monitor);
				if(!SPINUtil.isRootClass(cls) && !thisUnbound) {
					Set<Resource> subClasses = JenaUtil.getAllSubClasses(cls);
					for(Resource subClass : subClasses) {
						changed |= runCommandOnClass(arqWrapper, arqWrapper.getLabel(), queryModel, newTriples, subClass, true, class2Constructor, statistics, explanations, explanationText, newRules, thisUnbound, monitor);
					}
				}
			}
			iteration++;
			
			if(!newRules.isEmpty() && !singlePass) {
				for(Statement s : newRules) {
					SPINQueryFinder.add(class2Query, queryModel.asStatement(s.asTriple()), queryModel, true, false);
				}
			}
		}
		while(!singlePass && changed);
		
		return iteration - 1;
	}

	
	private static boolean runCommandOnClass(
			CommandWrapper commandWrapper, 
			String queryLabel, 
			final Model queryModel, 
			Model newTriples, 
			Resource cls, 
			boolean checkContains, 
			Map<Resource, List<CommandWrapper>> class2Constructor,
			List<SPINStatistics> statistics, 
			SPINExplanations explanations, 
			String explanationText, 
			Set<Statement> newRules, 
			boolean thisUnbound, 
			ProgressMonitor monitor) {
		
		// Check if query is needed at all
		if(thisUnbound || SPINUtil.isRootClass(cls) || queryModel.contains(null, RDF.type, cls)) {
			boolean changed = false;
			QuerySolutionMap bindings = new QuerySolutionMap();
			boolean needsClass = !SPINUtil.isRootClass(cls) && !thisUnbound;
			Map<String,RDFNode> initialBindings = commandWrapper.getTemplateBinding();
			if(initialBindings != null) {
				for(String varName : initialBindings.keySet()) {
					RDFNode value = initialBindings.get(varName);
					bindings.add(varName, value);
				}
			}
			long startTime = System.currentTimeMillis();
			final Map<Resource,Resource> newInstances = new HashMap<Resource,Resource>();
			if(commandWrapper instanceof QueryWrapper) {
				Query arq = ((QueryWrapper)commandWrapper).getQuery();
				Model cm;
				if(commandWrapper.isThisDeep() && needsClass) {
					
					// If there is no simple way to bind ?this inside of the query then
					// do the iteration over all instances in an "outer" loop
					cm = JenaUtil.createDefaultModel();
					StmtIterator it = queryModel.listStatements(null, RDF.type, cls);
					while(it.hasNext()) {
						Resource instance = it.next().getSubject();
						QueryExecution qexec = ARQFactory.get().createQueryExecution(arq, queryModel);
						bindings.add(SPIN.THIS_VAR_NAME, instance);
						qexec.setInitialBinding(bindings);
						qexec.execConstruct(cm);
						qexec.close();
					}
				}
				else {
					if(needsClass) {
						bindings.add(SPINUtil.TYPE_CLASS_VAR_NAME, cls);
					}
					QueryExecution qexec = ARQFactory.get().createQueryExecution(arq, queryModel, bindings);
					cm = qexec.execConstruct();
					qexec.close();
				}
				StmtIterator cit = cm.listStatements();
				while(cit.hasNext()) {
					Statement s = cit.next();
					if(!checkContains || !queryModel.contains(s)) {
						changed = true;
						newTriples.add(s);
						if(explanations != null && commandWrapper.getStatement() != null) {
							Resource source = commandWrapper.getStatement().getSubject();
							explanations.put(s.asTriple(), explanationText, source.asNode());
						}
						
						// New rdf:type triple -> run constructors later
						if(RDF.type.equals(s.getPredicate()) && s.getObject().isResource()) {
							Resource subject = s.getSubject().inModel(queryModel);
							newInstances.put(subject, s.getResource());
						}
						
						if(SPIN.rule.equals(s.getPredicate())) {
							newRules.add(s);
						}
					}
				}
			}
			else {
				UpdateWrapper updateWrapper = (UpdateWrapper) commandWrapper;
				Map<String,RDFNode> templateBindings = commandWrapper.getTemplateBinding();
				Dataset dataset = ARQFactory.get().getDataset(queryModel);
				Update update = updateWrapper.getUpdate();
				Iterable<Graph> updateGraphs = UpdateUtil.getUpdatedGraphs(update, dataset, templateBindings);
				ControlledUpdateGraphStore cugs = new ControlledUpdateGraphStore(dataset, updateGraphs);
				
				if(commandWrapper.isThisDeep() && needsClass) {
					for(Statement s : queryModel.listStatements(null, RDF.type, cls).toList()) {
						Resource instance = s.getSubject();
						bindings.add(SPIN.THIS_VAR_NAME, instance);
						UpdateProcessor up = UpdateExecutionFactory.create(update, cugs, JenaUtil.asBinding(bindings));
						up.execute();
					}
				}
				else {
					if(needsClass) {
						bindings.add(SPINUtil.TYPE_CLASS_VAR_NAME, cls);
					}
					UpdateProcessor up = UpdateExecutionFactory.create(update, cugs, JenaUtil.asBinding(bindings));
					up.execute();
				}
				
				for(ControlledUpdateGraph cug : cugs.getControlledUpdateGraphs()) {
					changed |= cug.isChanged();
					for(Triple triple : cug.getAddedTriples()) {
						if(RDF.type.asNode().equals(triple.getPredicate()) && !triple.getObject().isLiteral()) {
							Resource subject = (Resource) queryModel.asRDFNode(triple.getSubject());
							newInstances.put(subject, (Resource)queryModel.asRDFNode(triple.getObject()));
						}
					}
				}
			}
			
			if(statistics != null) {
				long endTime = System.currentTimeMillis();
				long duration = (endTime - startTime);
				Command spinCommand = commandWrapper.getSPINCommand();
				String queryText = spinCommand != null ? SPINLabels.get().getLabel(spinCommand) : commandWrapper.getLabel();
				if(queryLabel == null) {
					queryLabel = queryText;
				}
				statistics.add(new SPINStatistics(queryLabel, queryText, duration, startTime, cls.asNode()));
			}
			
			if(!newInstances.isEmpty()) {
				List<Resource> newRs = new ArrayList<Resource>(newInstances.keySet());
				SPINConstructors.construct(
						queryModel, 
						newRs, 
						newTriples, 
						new HashSet<Resource>(), 
						class2Constructor,
						statistics,
						explanations, 
						monitor);
			}
			
			return changed;
		}
		else {
			return false;
		}
	}

	
	/**
	 * Runs a given Jena Query on a given instance and adds the inferred triples
	 * to a given Model.
	 * @param queryWrapper  the wrapper of the CONSTRUCT query to execute
	 * @param queryModel  the query Model
	 * @param newTriples  the Model to write the triples to
	 * @param instance  the instance to run the inferences on
	 * @param checkContains  true to only call add if a Triple wasn't there yet
	 * @return true if changes were done (only meaningful if checkContains == true)
	 */
	public static boolean runQueryOnInstance(QueryWrapper queryWrapper, Model queryModel, Model newTriples, Resource instance, boolean checkContains) {
		boolean changed = false;
		QueryExecution qexec = ARQFactory.get().createQueryExecution(queryWrapper.getQuery(), queryModel);
		QuerySolutionMap bindings = new QuerySolutionMap();
		bindings.add(SPIN.THIS_VAR_NAME, instance);
		Map<String,RDFNode> initialBindings = queryWrapper.getTemplateBinding();
		if(initialBindings != null) {
			for(String varName : initialBindings.keySet()) {
				RDFNode value = initialBindings.get(varName);
				bindings.add(varName, value);
			}
		}
		qexec.setInitialBinding(bindings);
		Model cm = qexec.execConstruct();
		StmtIterator cit = cm.listStatements();
		while(cit.hasNext()) {
			Statement s = cit.nextStatement();
			if(!checkContains || !queryModel.contains(s)) {
				changed = true;
				newTriples.add(s);
			}
		}
		return changed;
	}
}
