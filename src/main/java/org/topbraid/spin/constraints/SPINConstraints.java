/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.constraints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Argument;
import org.topbraid.spin.model.Ask;
import org.topbraid.spin.model.Construct;
import org.topbraid.spin.model.ElementList;
import org.topbraid.spin.model.QueryOrTemplateCall;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.SPINInstance;
import org.topbraid.spin.model.Template;
import org.topbraid.spin.model.TemplateCall;
import org.topbraid.spin.progress.ProgressMonitor;
import org.topbraid.spin.statistics.SPINStatistics;
import org.topbraid.spin.system.SPINImports;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.util.CommandWrapper;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.PropertyPathsGetter;
import org.topbraid.spin.util.QueryWrapper;
import org.topbraid.spin.util.SPINQueryFinder;
import org.topbraid.spin.util.SPINUtil;
import org.topbraid.spin.vocabulary.SP;
import org.topbraid.spin.vocabulary.SPIN;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;


/**
 * Performs SPIN constraint checking on one or more instances, based
 * on the spin:constraints defined on the types of those instances.
 * 
 * @author Holger Knublauch
 */
public class SPINConstraints {
	
	private static List<TemplateCall> NO_FIXES = Collections.emptyList();
	

	public static void addConstraintViolations(List<ConstraintViolation> results, SPINInstance instance, Property predicate, boolean matchValue, List<SPINStatistics> stats, ProgressMonitor monitor) {
		if(predicate == null) {
			predicate = SPIN.constraint;
		}
		List<QueryOrTemplateCall> qots = instance.getQueriesAndTemplateCalls(predicate);
		for(QueryOrTemplateCall qot : qots) {
			if(qot.getTemplateCall() != null) {
				addTemplateCallResults(results, qot, instance, matchValue, monitor);
			}
			else if(qot.getQuery() != null) {
				addQueryResults(results, qot, instance, matchValue, stats, monitor);
			}
		}
	}


	/**
	 * Creates an RDF representation (instances of spin:ConstraintViolation) from a
	 * collection of ConstraintViolation Java objects. 
	 * @param cvs  the violation objects
	 * @param result  the Model to add the results to
	 * @param createSource  true to also create the spin:violationSource
	 */
	public static void addConstraintViolationsRDF(List<ConstraintViolation> cvs, Model result, boolean createSource) {
		for(ConstraintViolation cv : cvs) {
			Resource r = result.createResource(SPIN.ConstraintViolation);
			String message = cv.getMessage();
			if(message != null && message.length() > 0) {
				r.addProperty(RDFS.label, message);
			}
			if(cv.getRoot() != null) {
				r.addProperty(SPIN.violationRoot, cv.getRoot());
			}
			r.addProperty(SPIN.violationLevel, cv.getLevel());
			for(SimplePropertyPath path : cv.getPaths()) {
				if(path instanceof ObjectPropertyPath) {
					r.addProperty(SPIN.violationPath, path.getPredicate());
				}
				else {
					Resource p = result.createResource(SP.ReversePath);
					p.addProperty(SP.path, path.getPredicate());
					r.addProperty(SPIN.violationPath, p);
				}
			}
			if(createSource && cv.getSource() != null) {
				r.addProperty(SPIN.violationSource, cv.getSource());
			}
			if(cv.getValue() != null) {
				r.addProperty(SPIN.violationValue, cv.getValue());
			}
		}
	}

	
	private static void addConstructedProblemReports(
			Model cm,
			List<ConstraintViolation> results,
			Model model,
			Resource atClass,
			Resource matchRoot,
			String label,
			Resource source) {
		StmtIterator it = cm.listStatements(null, RDF.type, SPIN.ConstraintViolation);
		while(it.hasNext()) {
			Statement s = it.nextStatement();
			Resource vio = s.getSubject();
			
			Resource root = null;
			Statement rootS = vio.getProperty(SPIN.violationRoot);
			if(rootS != null && rootS.getObject().isResource()) {
				root = rootS.getResource().inModel(model);
			}
			if(matchRoot == null || matchRoot.equals(root)) {
				
				Statement labelS = vio.getProperty(RDFS.label);
				if(labelS != null && labelS.getObject().isLiteral()) {
					label = labelS.getString();
				}
				else if(label == null) {
					label = "SPIN constraint at " + SPINLabels.get().getLabel(atClass);
				}
				
				List<SimplePropertyPath> paths = getViolationPaths(model, vio, root);
				List<TemplateCall> fixes = getFixes(cm, model, vio);
				results.add(createConstraintViolation(paths, JenaUtil.getProperty(vio, SPIN.violationValue),
						fixes, root, label, source, JenaUtil.getPropertyResourceValue(vio, SPIN.violationLevel)));
			}
		}
	}


	public static void addQueryResults(List<ConstraintViolation> results, QueryOrTemplateCall qot, Resource resource, boolean matchValue, List<SPINStatistics> stats, ProgressMonitor monitor) {
		
		QuerySolutionMap arqBindings = new QuerySolutionMap();
		
		String queryString = ARQFactory.get().createCommandString(qot.getQuery());
		arqBindings.add(SPIN.THIS_VAR_NAME, resource);
		
		Query arq = ARQFactory.get().createQuery(queryString);
		Model model = resource.getModel();
		QueryExecution qexec = ARQFactory.get().createQueryExecution(arq, model);
		
		qexec.setInitialBinding(arqBindings);
		
		long startTime = System.currentTimeMillis();
		if(arq.isAskType()) {
			if(qexec.execAsk() != matchValue) {
				String message;
				String comment = qot.getQuery().getComment();
				if(comment == null) {
					comment = JenaUtil.getStringProperty(qot.getQuery(), RDFS.label);
				}
				if(comment == null) {
					message = SPINLabels.get().getLabel(qot.getQuery());
				}
				else {
					message = comment;
				}
				message += "\n(SPIN constraint at " + SPINLabels.get().getLabel(qot.getCls()) + ")";
				List<SimplePropertyPath> paths;
				Resource path = JenaUtil.getPropertyResourceValue(qot.getQuery(), SPIN.violationPath);
				if(path != null && path.isURIResource()) {
					paths = new ArrayList<SimplePropertyPath>(1);
					paths.add(new ObjectPropertyPath(resource, JenaUtil.asProperty(path)));
				}
				else {
					paths = getPropertyPaths(resource, qot.getQuery().getWhere(), null);
				}
				Resource source = getSource(qot);
				results.add(createConstraintViolation(paths, null, NO_FIXES, resource, message, source, null));
			}
		}
		else if(arq.isConstructType()) {
			Model cm = qexec.execConstruct();
			qexec.close();
			addConstructedProblemReports(cm, results, model, qot.getCls(), resource, qot.getQuery().getComment(), getSource(qot));
		}
		long endTime = System.currentTimeMillis();
		if(stats != null) {
			long duration = startTime - endTime;
			String label = qot.toString();
			String queryText;
			if(qot.getTemplateCall() != null) {
				queryText = SPINLabels.get().getLabel(qot.getTemplateCall().getTemplate().getBody());
			}
			else {
				queryText = SPINLabels.get().getLabel(qot.getQuery());
			}
			Node cls = qot.getCls() != null ? qot.getCls().asNode() : null;
			stats.add(new SPINStatistics(label, queryText, duration, startTime, cls));
		}
	}


	public static void addTemplateCallResults(List<ConstraintViolation> results, QueryOrTemplateCall qot,
			Resource resource, boolean matchValue, ProgressMonitor monitor) {
		TemplateCall templateCall = qot.getTemplateCall();
		Template template = templateCall.getTemplate();
		addTemplateCallResults(results, qot, resource, matchValue, templateCall, template);
		for(Resource superClass : JenaUtil.getAllSuperClasses(template)) {
			if(JenaUtil.hasIndirectType(superClass, SPIN.Template)) {
				addTemplateCallResults(results, qot, resource, matchValue, templateCall, SPINFactory.asTemplate(superClass));
			}
		}
	}


	private static void addTemplateCallResults(List<ConstraintViolation> results,
			QueryOrTemplateCall qot, Resource resource, boolean matchValue,
			TemplateCall templateCall, Template template) {
		if(template != null && template.getBody() instanceof org.topbraid.spin.model.Query) {
			org.topbraid.spin.model.Query spinQuery = (org.topbraid.spin.model.Query) template.getBody();
			if(spinQuery instanceof Ask || spinQuery instanceof Construct) {

				QuerySolutionMap bindings = createInitialBindings(resource, templateCall);
				for(Argument arg : template.getArguments(false)) {
					if(!arg.isOptional()) {
						if(!bindings.contains(arg.getVarName())) {
							// Don't execute this template if any non-optional argument is missing
							return;
						}
					}
				}
				
				Model model = resource.getModel();
				Query arq = ARQFactory.get().createQuery(spinQuery);
				QueryExecution qexec = ARQFactory.get().createQueryExecution(arq, model);
				qexec.setInitialBinding(bindings);
				
				if(spinQuery instanceof Ask) {
					if(qexec.execAsk() != matchValue) {
						List<SimplePropertyPath> paths = getPropertyPaths(resource, spinQuery.getWhere(), templateCall.getArgumentsMapByProperties());
						String message = SPINLabels.get().getLabel(templateCall);
						message += "\n(SPIN constraint at " + SPINLabels.get().getLabel(qot.getCls()) + ")";
						results.add(createConstraintViolation(paths, null, NO_FIXES, resource, message, templateCall, null));
					}
				}
				else if(spinQuery instanceof Construct) {
					Model cm = qexec.execConstruct();
					qexec.close();
					Resource source = getSource(qot);
					String label = SPINLabels.get().getLabel(templateCall);
					addConstructedProblemReports(cm, results, model, qot.getCls(), resource, label, source);
				}
			}
		}
	}

	
	/**
	 * Checks all spin:constraints for a given Resource.
	 * @param resource  the instance to run constraint checks on
	 * @param monitor  an (optional) progress monitor (currently ignored)
	 * @return a List of ConstraintViolations (empty if all is OK)
	 */
	public static List<ConstraintViolation> check(Resource resource, ProgressMonitor monitor) {
		return check(resource, SPIN.constraint, new LinkedList<SPINStatistics>(), monitor);
	}

	
	/**
	 * Checks all spin:constraints for a given Resource.
	 * @param resource  the instance to run constraint checks on
	 * @param predicate  the system property, e.g. a sub-property of spin:constraint 
	 *                   or null for the default (spin:constraint)
	 * @param monitor  an (optional) progress monitor (currently ignored)
	 * @return a List of ConstraintViolations (empty if all is OK)
	 */
	public static List<ConstraintViolation> check(Resource resource, Property predicate, ProgressMonitor monitor) {
		return check(resource, predicate, new LinkedList<SPINStatistics>(), monitor);
	}

	
	/**
	 * Checks all spin:constraints for a given Resource.
	 * @param resource  the instance to run constraint checks on
	 * @param predicate  the system property, i.e. spin:constraint or a sub-property thereof
	 *                   or null for the default (spin:constraint)
	 * @param stats  an (optional) List to add statistics to
	 * @param monitor  an (optional) progress monitor (currently ignored)
	 * @return a List of ConstraintViolations (empty if all is OK)
	 */
	public static List<ConstraintViolation> check(Resource resource, Property predicate, List<SPINStatistics> stats, ProgressMonitor monitor) {
		List<ConstraintViolation> results = new LinkedList<ConstraintViolation>();
		
		// If spin:imports exist, then continue with the union model
		try {
			Model importsModel = SPINImports.get().getImportsModel(resource.getModel());
			if(importsModel != resource.getModel()) {
				resource = resource.inModel(importsModel);
			}
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}
		
		SPINInstance instance = resource.as(SPINInstance.class);
		addConstraintViolations(results, instance, predicate, false, stats, monitor);
		return results;
	}
	

	/**
	 * Checks all instances in a given Model against all spin:constraints and
	 * returns a List of constraint violations. 
	 * A ProgressMonitor can be provided to enable the user to get intermediate
	 * status reports and to cancel the operation.
	 * @param model  the Model to operate on
	 * @param monitor  an optional ProgressMonitor
	 * @return a List of ConstraintViolations
	 */
	public static List<ConstraintViolation> check(Model model, ProgressMonitor monitor) {
		return check(model, SPIN.constraint, null, monitor);
	}
	

	/**
	 * Checks all instances in a given Model against all spin:constraints and
	 * returns a List of constraint violations. 
	 * A ProgressMonitor can be provided to enable the user to get intermediate
	 * status reports and to cancel the operation.
	 * @param model  the Model to operate on
	 * @param monitor  an optional ProgressMonitor
	 * @return a List of ConstraintViolations
	 */
	public static List<ConstraintViolation> check(Model model, Property predicate, ProgressMonitor monitor) {
		return check(model, predicate, null, monitor);
	}

	
	/**
	 * Checks all instances in a given Model against all spin:constraints and
	 * returns a List of constraint violations. 
	 * A ProgressMonitor can be provided to enable the user to get intermediate
	 * status reports and to cancel the operation.
	 * @param model  the Model to operate on
	 * @param predicate  the system property, e.g. a sub-property of spin:constraint
	 * @param stats  an (optional) List to write statistics reports to
	 * @param monitor  an optional ProgressMonitor
	 * @return a List of ConstraintViolations
	 */
	public static List<ConstraintViolation> check(Model model, Property predicate, List<SPINStatistics> stats, ProgressMonitor monitor) {
		List<ConstraintViolation> results = new LinkedList<ConstraintViolation>();
		run(model, predicate, results, stats, monitor);
		return results;
	}
	
	
	private static synchronized Query convertAskToConstruct(Query ask, org.topbraid.spin.model.Query spinQuery, String label) {
		Syntax oldSyntax = Syntax.defaultSyntax; // Work-around to bug in ARQ
		try {
		    Syntax.defaultSyntax = ask.getSyntax();
			Query construct = org.apache.jena.query.QueryFactory.create(ask);
			construct.setQueryConstructType();
			BasicPattern bgp = new BasicPattern();
			Node cv = NodeFactory.createAnon();
			bgp.add(Triple.create(cv, RDF.type.asNode(), SPIN.ConstraintViolation.asNode()));
			Node thisVar = Var.alloc(SPIN.THIS_VAR_NAME);
			bgp.add(Triple.create(cv, SPIN.violationRoot.asNode(), thisVar));
			if(label == null) {
				label = spinQuery.getComment();
			}
			if(label == null) {
				label = JenaUtil.getStringProperty(spinQuery, RDFS.label);
			}
			if(label != null) {
				bgp.add(Triple.create(cv, RDFS.label.asNode(), NodeFactory.createLiteral(label)));
			}
			Resource path = JenaUtil.getResourceProperty(spinQuery, SPIN.violationPath);
			if(path != null && path.isURIResource()) {
				bgp.add(Triple.create(cv, SPIN.violationPath.asNode(), path.asNode()));
			}
			org.apache.jena.sparql.syntax.Template template = new org.apache.jena.sparql.syntax.Template(bgp);
			construct.setConstructTemplate(template);
			Element where = construct.getQueryPattern();
			construct.setQueryPattern(where);
			return construct;
		}
		finally {
			Syntax.defaultSyntax = oldSyntax;
		}
	}


	private static ConstraintViolation createConstraintViolation(Collection<SimplePropertyPath> paths,
			RDFNode value,
			Collection<TemplateCall> fixes, 
			Resource instance, 
			String message, 
			Resource source,
			Resource level) {
		ConstraintViolation result = new ConstraintViolation(instance, paths, fixes, message, source);
		result.setValue(value);
		result.setLevel(level);
		return result;
	}


	private static QuerySolutionMap createInitialBindings(Resource resource, TemplateCall templateCall) {
		QuerySolutionMap arqBindings = new QuerySolutionMap();
		arqBindings.add(SPIN.THIS_VAR_NAME, resource);
		Map<Argument,RDFNode> args = templateCall.getArgumentsMap();
		for(Argument arg : args.keySet()) {
			RDFNode value = args.get(arg);
			arqBindings.add(arg.getVarName(), value);
		}
		return arqBindings;
	}


	private static List<TemplateCall> getFixes(Model cm, Model model, Resource vio) {
		List<TemplateCall> fixes = new ArrayList<TemplateCall>();
		Iterator<Statement> fit = vio.listProperties(SPIN.fix);
		while(fit.hasNext()) {
			Statement fs = fit.next();
			if(fs.getObject().isResource()) {
				MultiUnion union = JenaUtil.createMultiUnion(new Graph[] {
						model.getGraph(),
						cm.getGraph()
				});
				Model unionModel = ModelFactory.createModelForGraph(union);
				Resource r = fs.getResource().inModel(unionModel);
				TemplateCall fix = SPINFactory.asTemplateCall(r);
				fixes.add(fix);
			}
		}
		return fixes;
	}


	private static List<SimplePropertyPath> getPropertyPaths(Resource resource, ElementList where, Map<Property,RDFNode> varBindings) {
		if(where != null) {
			PropertyPathsGetter getter = new PropertyPathsGetter(where, varBindings);
			getter.run();
			return new ArrayList<SimplePropertyPath>(getter.getResults());
		}
		else {
			return Collections.emptyList();
		}
	}
	
	
	private static Resource getSource(QueryOrTemplateCall qot) {
		if(qot.getQuery() != null) {
			return qot.getQuery();
		}
		else {
			return qot.getTemplateCall();
		}
	}


	private static List<SimplePropertyPath> getViolationPaths(Model model, Resource vio, Resource root) {
		List<SimplePropertyPath> paths = new ArrayList<SimplePropertyPath>();
		StmtIterator pit = vio.listProperties(SPIN.violationPath);
		while(pit.hasNext()) {
			Statement p = pit.nextStatement();
			if(p.getObject().isURIResource()) {
				Property predicate = model.getProperty(p.getResource().getURI());
				paths.add(new ObjectPropertyPath(root, predicate));
			}
			else if(p.getObject().isAnon()) {
				Resource path = p.getResource();
				if(path.hasProperty(RDF.type, SP.ReversePath)) {
					Statement reverse = path.getProperty(SP.path);
					if(reverse != null && reverse.getObject().isURIResource()) {
						Property predicate = model.getProperty(reverse.getResource().getURI());
						paths.add(new SubjectPropertyPath(root, predicate));
					}
				}
			}
		}
		return paths;
	}
	
	
	/**
	 * Checks if a given property is a SPIN constraint property.
	 * This is defined as a property that is spin:constraint or a sub-property of it.
	 * @param property  the property to check
	 * @return true if property is a constraint property
	 */
	public static boolean isConstraintProperty(Property property) {
		if(SPIN.constraint.equals(property)) {
			return true;
		}
		else if(JenaUtil.hasSuperProperty(property, property.getModel().getProperty(SPIN.constraint.getURI()))) {
			return true;
		}
		else {
			return false; 
		}
	}

	
	private static void run(Model model, Property predicate, List<ConstraintViolation> results, List<SPINStatistics> stats, ProgressMonitor monitor) {
		
		if(predicate == null) {
			predicate = SPIN.constraint;
		}
		
		// If spin:imports exist then continue with the union model
		try {
			model = SPINImports.get().getImportsModel(model);
		}
		catch(IOException ex) {
			// TODO: better error handling
			ex.printStackTrace();
		}
		if(monitor != null) {
			monitor.setTaskName("Preparing SPIN Constraints");
		}
		Map<Resource,List<CommandWrapper>> class2Query = SPINQueryFinder.getClass2QueryMap(model, model, predicate, true, true);

		if(monitor != null) {
			int totalWork = 0;
			for(Resource cls : class2Query.keySet()) {
				List<CommandWrapper> arqs = class2Query.get(cls);
				totalWork += arqs.size() + 1;
			}
			monitor.beginTask("Checking SPIN Constraints on " + class2Query.size() + " classes", totalWork);
		}
		for(Resource cls : class2Query.keySet()) {
			List<CommandWrapper> arqs = class2Query.get(cls);
			for(CommandWrapper arqWrapper : arqs) {
				QueryWrapper queryWrapper = (QueryWrapper) arqWrapper;
				Query arq = queryWrapper.getQuery();
				String label = arqWrapper.getLabel();
				if(arq.isAskType()) {
					arq = convertAskToConstruct(arq, queryWrapper.getSPINQuery(), label);
				}
				runQueryOnClass(results, arq, queryWrapper.getSPINQuery(), label, model, cls, queryWrapper.getTemplateBinding(), arqWrapper.isThisUnbound(), arqWrapper.isThisDeep(), arqWrapper.getSource(), stats, monitor);
				if(!arqWrapper.isThisUnbound()) {
					Set<Resource> subClasses = JenaUtil.getAllSubClasses(cls);
					for(Resource subClass : subClasses) {
						runQueryOnClass(results, arq, queryWrapper.getSPINQuery(), label, model, subClass, queryWrapper.getTemplateBinding(), arqWrapper.isThisUnbound(), arqWrapper.isThisDeep(), arqWrapper.getSource(), stats, monitor);
					}
				}
				if(monitor != null) {
					monitor.worked(1);
					if(monitor.isCanceled()) {
						return;
					}
				}
			}
			if(monitor != null) {
				monitor.worked(1);
			}
		}
	}
	
	
	private static void runQueryOnClass(List<ConstraintViolation> results, Query arq, org.topbraid.spin.model.Query spinQuery, String label, Model model, Resource cls, Map<String,RDFNode> initialBindings, boolean thisUnbound, boolean thisDeep, Resource source, List<SPINStatistics> stats, ProgressMonitor monitor) {
		if(thisUnbound || SPINUtil.isRootClass(cls) || model.contains(null, RDF.type, cls)) {
			QuerySolutionMap arqBindings = new QuerySolutionMap();
			if(!thisUnbound) {
				arqBindings.add(SPINUtil.TYPE_CLASS_VAR_NAME, cls);
			}
			if(initialBindings != null) {
				for(String varName : initialBindings.keySet()) {
					RDFNode value = initialBindings.get(varName);
					arqBindings.add(varName, value);
				}
			}
			
			if(monitor != null) {
				monitor.subTask("Checking SPIN constraint on " + SPINLabels.get().getLabel(cls) + (label != null ? ": " + label : ""));
			}
			
			long startTime = System.currentTimeMillis();
			Model cm = JenaUtil.createDefaultModel();
			if(thisDeep && !thisUnbound) {
				StmtIterator it = model.listStatements(null, RDF.type, cls);
				while(it.hasNext()) {
					Resource instance = it.next().getSubject();
					arqBindings.add(SPIN.THIS_VAR_NAME, instance);
					QueryExecution qexec = ARQFactory.get().createQueryExecution(arq, model, arqBindings);
					qexec.execConstruct(cm);
					qexec.close();
				}
			}
			else {
				QueryExecution qexec = ARQFactory.get().createQueryExecution(arq, model, arqBindings);
				qexec.execConstruct(cm);
				qexec.close();
			}
			
			long endTime = System.currentTimeMillis();
			if(stats != null) {
				long duration = endTime - startTime;
				String queryText = SPINLabels.get().getLabel(spinQuery);
				if(label == null) {
					label = queryText;
				}
				stats.add(new SPINStatistics(label, queryText, duration, startTime, cls.asNode()));
			}
			addConstructedProblemReports(cm, results, model, cls, null, label, source);
		}
	}
}
