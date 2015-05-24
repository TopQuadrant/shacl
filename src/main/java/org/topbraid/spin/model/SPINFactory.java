/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.topbraid.spin.arq.Aggregations;
import org.topbraid.spin.model.impl.TriplePatternImpl;
import org.topbraid.spin.model.update.Clear;
import org.topbraid.spin.model.update.Create;
import org.topbraid.spin.model.update.Delete;
import org.topbraid.spin.model.update.DeleteData;
import org.topbraid.spin.model.update.DeleteWhere;
import org.topbraid.spin.model.update.Drop;
import org.topbraid.spin.model.update.Insert;
import org.topbraid.spin.model.update.InsertData;
import org.topbraid.spin.model.update.Load;
import org.topbraid.spin.model.update.Modify;
import org.topbraid.spin.model.update.Update;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.vocabulary.SP;
import org.topbraid.spin.vocabulary.SPIN;
import org.topbraid.spin.vocabulary.SPL;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.vocabulary.RDF;


/**
 * The singleton that is used to convert plain Jena objects into
 * SPIN API resources, and to do corresponding tests.
 * 
 * @author Holger Knublauch
 */
@SuppressWarnings("deprecation")
public class SPINFactory {
	
	/**
	 * Attempts to cast a given Resource into an Aggregation.
	 * Resources that have an aggregation type as their rdf:type
	 * are recognized as well-formed aggregations.
	 * @param resource  the Resource to cast
	 * @return the Aggregation or null if Resource is not a well-formed aggregation
	 */
	public static Aggregation asAggregation(Resource resource) {
		StmtIterator it = resource.listProperties(RDF.type);
		JenaUtil.setGraphReadOptimization(true);
		try {
			while(it.hasNext()) {
				RDFNode type = it.next().getObject();
				if(type instanceof Resource) {
					if(Aggregations.getName((Resource)type) != null) {
						it.close();
						return resource.as(Aggregation.class);
					}
				}
			}
		}
		finally {
			JenaUtil.setGraphReadOptimization(false);
		}
		return null;
	}
	
	
	/**
	 * Attempts to cast a given Resource into the most specific
	 * subclass of Command, esp Update or Query.
	 * @param resource  the Resource to cast
	 * @return resource cast into the best possible type or null
	 */
	public static Command asCommand(Resource resource) {
		Query query = asQuery(resource);
		if(query != null) {
			return query;
		}
		else {
			return asUpdate(resource);
		}
	}

	
	/**
	 * Checks whether a given Resource represents a SPARQL element, and returns
	 * an instance of a subclass of Element if so.
	 * @param resource  the Resource to check
	 * @return Resource as an Element or null if resource is not an element
	 */
	public static Element asElement(Resource resource) {
		final TriplePattern triplePattern = asTriplePattern(resource);
		if(triplePattern != null) {
			return triplePattern;
		}
		else if(resource.canAs(TriplePath.class)) {
			return resource.as(TriplePath.class);
		}
		else if(resource.canAs(Filter.class)) {
			return resource.as(Filter.class);
		}
		else if(resource.canAs(Bind.class)) {
			return resource.as(Bind.class);
		}
		else if(resource.canAs(Optional.class)) {
			return resource.as(Optional.class);
		}
		else if(resource.canAs(NamedGraph.class)) {
			return resource.as(NamedGraph.class); 
		}
		else if(resource.canAs(Minus.class)) {
			return resource.as(Minus.class);
		}
		else if(resource.canAs(Exists.class)) {
			return resource.as(Exists.class);
		}
		else if(resource.canAs(NotExists.class)) {
			return resource.as(NotExists.class);
		}
		else if(resource.canAs(Service.class)) {
			return resource.as(Service.class);
		}
		else if(resource.canAs(SubQuery.class)) {
			return resource.as(SubQuery.class); 
		}
		else if(resource.canAs(Union.class)) {
			return resource.as(Union.class); 
		}
		else if(resource.canAs(Values.class)) {
			return resource.as(Values.class);
		}
		else if(isElementList(resource)) {
			return resource.as(ElementList.class);
		}
		else {
			return null;
		}
	}

	
	/**
	 * Returns the most specific Java instance for a given RDFNode.
	 * If the node is an aggregation, it will be returned as instance of
	 * Aggregation.
	 * If the node is a function call, it will be returned as instance of
	 * FunctionCall.
	 * If it's a Variable, the Variable will be returned.
	 * Otherwise the node itself will be returned.
	 * @param node  the node to cast
	 * @return node or node as a Function or Variable
	 */
	public static RDFNode asExpression(RDFNode node) {
		if(node instanceof Resource) {
			Variable var = SPINFactory.asVariable(node);
			if(var != null) {
				return var;
			}
			Aggregation aggr = SPINFactory.asAggregation((Resource)node);
			if(aggr != null) {
				return aggr;
			}
			FunctionCall functionCall = SPINFactory.asFunctionCall((Resource)node);
			if(functionCall != null) {
				return functionCall;
			}
		}
		return node;
	}
	
	
	/**
	 * Converts a given Resource into a Function instance.
	 * No other tests are done.
	 * @param resource  the Resource to convert
	 * @return the Function
	 */
	public static Function asFunction(Resource resource) {
		return resource.as(Function.class);
	}

	
	/**
	 * Checks if a given Resource might represent a Function call, and if
	 * yes returns the resource as Function.  The condition here is fairly
	 * general: a function call must be a blank node with an rdf:type triple
	 * where the type triple's object is a URI resource.  It is generally
	 * assumed that this function is called after other options have been
	 * exhausted.  For example, in order to test whether a resource is a
	 * variable or a function call, the variable test must be done first
	 * as it is more specific than these test conditions 
	 * @param resource  the Resource to test
	 * @return resource as a Function or null if resource cannot be cast
	 */
	public static FunctionCall asFunctionCall(Resource resource) {
		if(resource.isAnon()) {
			Statement s = resource.getProperty(RDF.type);
			if(s != null && s.getObject().isURIResource() && !SP.Variable.equals(s.getObject())) {
				return resource.as(FunctionCall.class);
			}
		}
	    return null;
	}

	
	/**
	 * Checks if a given Resource is a SPIN query, and returns an
	 * instance of a subclass of Query if so. 
	 * @param resource  the Resource to test
	 * @return resource as a Query or null
	 */
	public static Query asQuery(Resource resource) {
		if(resource.canAs(Select.class)) {
			return resource.as(Select.class);
		}
		else if(resource.canAs(Construct.class)) {
			return resource.as(Construct.class);
		}
		else if(resource.canAs(Ask.class)) {
			return resource.as(Ask.class);
		}
		else if(resource.canAs(Describe.class)) {
			return resource.as(Describe.class);
		}
		else {
			return null;
		}
	}
	
	
	/**
	 * Converts a given Resource into a Template instance.
	 * No other tests are done.
	 * @param resource  the Resource to convert
	 * @return the Template
	 */
	public static Template asTemplate(Resource resource) {
		return resource.as(Template.class);
	}
	
	
	/**
	 * Checks whether a given RDFNode can be cast into TemplateCall, and returns
	 * it as a TemplateCall instance if so.
	 * @param node  the node to convert
	 * @return an instance of TemplateCall or null
	 */
	public static TemplateCall asTemplateCall(RDFNode node) {
		if(node instanceof Resource) {
			Statement s = ((Resource)node).getProperty(RDF.type);
			if(s != null && s.getObject().isURIResource()) {
				String uri = s.getResource().getURI();
				Template template = SPINModuleRegistry.get().getTemplate(uri, s.getModel());
				if(template != null) {
					return node.as(TemplateCall.class);
				}
			}
		}
		return null;
	}

	
	/**
	 * Checks whether a given RDFNode can be converted into a TriplePattern, and if yes,
	 * returns an instance of TriplePattern.
	 * @param node  the node to test
	 * @return node as TriplePattern or null
	 */
	public static TriplePattern asTriplePattern(RDFNode node) {
		if(node instanceof Resource && ((Resource)node).hasProperty(SP.predicate)) {
			return new TriplePatternImpl(node.asNode(), (EnhGraph)((Resource)node).getModel());
		}
		else {
			return null;
		}
	}
	
	
	/**
	 * Checks if a given Resource is a subclass of sp:Update and
	 * casts it into the most specific Java class possible.
	 * @param resource  the Resource to cast
	 * @return the Update or null if resource cannot be cast
	 */
	public static Update asUpdate(Resource resource) {
		if(resource.canAs(Modify.class)) {
			return resource.as(Modify.class);
		}
		else if(resource.canAs(Clear.class)) {
			return resource.as(Clear.class);
		}
		else if(resource.canAs(Create.class)) {
			return resource.as(Create.class);
		}
		else if(resource.canAs(DeleteData.class)) {
			return resource.as(DeleteData.class);
		}
		else if(resource.canAs(DeleteWhere.class)) {
			return resource.as(DeleteWhere.class);
		}
		else if(resource.canAs(Drop.class)) {
			return resource.as(Drop.class);
		}
		else if(resource.canAs(InsertData.class)) {
			return resource.as(InsertData.class);
		}
		else if(resource.canAs(Load.class)) {
			return resource.as(Load.class);
		}
		else if(resource.canAs(Delete.class)) {
			return resource.as(Delete.class);
		}
		else if(resource.canAs(Insert.class)) {
			return resource.as(Insert.class);
		}
		else {
			return null;
		}
	}
	
	
	/**
	 * Checks whether a given RDFNode can be cast into a Variable and - if yes -
	 * converts it into an instance of Variable.  The Resource must have a value
	 * for spin:varName.
	 * @param node  the node to check
	 * @return resource as a Variable or null
	 */
	public static Variable asVariable(RDFNode node) {
		if(node instanceof Resource && ((Resource)node).hasProperty(SP.varName)) {
			return node.as(Variable.class);
		}
		else {
			return null;
		}
	}
	

	/**
	 * Creates an spl:Argument with a given property and value type.
	 * The new Argument resource will be a blank node in a given Model.
	 * @param model  the Model
	 * @param argProperty  the property or null
	 * @param argType  the value type or null
	 * @param optional  true if the Argument shall be optional
	 * @return the new Argument
	 */
	public static Argument createArgument(Model model, Property argProperty, Resource argType, boolean optional) {
		Argument a = model.createResource(SPL.Argument).as(Argument.class);
		if(argProperty != null) {
			a.addProperty(SPL.predicate, argProperty);
		}
		if(argType != null) {
			a.addProperty(SPL.valueType, argType);
		}
		if(optional) {
			a.addProperty(SPL.optional, model.createTypedLiteral(true));
		}
		return a;
	}
	

	/**
	 * Creates a new spl:Attribute as a blank node in a given Model.
	 * @param model  the Model to create the attribute in
	 * @param argProperty  the predicate or null
	 * @param argType  the value type or null
	 * @param minCount  the minimum cardinality or null
	 * @param maxCount  the maximum cardinality or null
	 * @return a new Attribute
	 */
	public static Attribute createAttribute(Model model, Property argProperty, Resource argType, Integer minCount, Integer maxCount) {
		Attribute a = model.createResource(SPL.Attribute).as(Attribute.class);
		if(argProperty != null) {
			a.addProperty(SPL.predicate, argProperty);
		}
		if(argType != null) {
			a.addProperty(SPL.valueType, argType);
		}
		if(minCount != null) {
			a.addProperty(SPL.minCount, model.createTypedLiteral(minCount.intValue()));
		}
		if(maxCount != null) {
			a.addProperty(SPL.maxCount, model.createTypedLiteral(maxCount.intValue()));
		}
		return a;
	}
	
	
	/**
	 * Creates an Ask query for a given WHERE clause.
	 * @param model  the Model to create the Ask (blank node) in
	 * @param where  the elements of the WHERE clause
	 * @return the new Ask query
	 */
	public static Ask createAsk(Model model, ElementList where) {
		Ask ask = model.createResource(SP.Ask).as(Ask.class);
		ask.addProperty(SP.where, where);
		return ask;
	}
	

	/**
	 * Creates a Bind in a given Model as a blank node.
	 * @param model  the Model to create the Bind in
	 * @param variable  the Variable to assign
	 * @param expression  the expression
	 * @return a new Bind instance
	 */
	public static Bind createBind(Model model, Variable variable, RDFNode expression) {
		Bind bind = model.createResource(SP.Bind).as(Bind.class);
		if(variable != null) {
			bind.addProperty(SP.variable, variable);
		}
		if(expression != null) {
			bind.addProperty(SP.expression, expression);
		}
		return bind;
	}


	/**
	 * Creates a new ElementList in a given Model.
	 * @param model  the Model to create the ElementList in
	 * @param elements  the elements (may be empty)
	 * @return a new ElementList (may be rdf:nil)
	 */
	public static ElementList createElementList(Model model, Element[] elements) {
		if(elements.length > 0) {
			return model.createList(elements).as(ElementList.class);
		}
		else {
			return RDF.nil.inModel(model).as(ElementList.class);
		}
	}
	
	
	/**
	 * Creates a new ElementList in a given Model.
	 * @param model  the Model to create the ElementList in
	 * @param elements  the elements (may be empty)
	 * @return a new ElementList (may be rdf:nil)
	 */
	public static ElementList createElementList(Model model, Iterator<Element> elements) {
		if(elements.hasNext()) {
			return model.createList(elements).as(ElementList.class);
		}
		else {
			return RDF.nil.inModel(model).as(ElementList.class);
		}
	}
	
	
	/**
	 * Creates a new Exists as a blank node in a given Model.
	 * @param model  the Model to create the EXISTS in
	 * @param elements  the elements of the EXISTS
	 * @return a new Exists
	 */
	public static Exists createExists(Model model, ElementList elements) {
		Exists notExists = model.createResource(SP.Exists).as(Exists.class);
		notExists.addProperty(SP.elements, elements);
		return notExists;
	}


	/**
	 * Creates a Filter from a given expression.
	 * @param model  the Model to create the (blank node) Filter in
	 * @param expression  the expression node (not null)
	 * @return a new Filter
	 */
	public static Filter createFilter(Model model, RDFNode expression) {
		Filter filter = model.createResource(SP.Filter).as(Filter.class);
		filter.addProperty(SP.expression, expression);
		return filter;
	}
	

	/**
	 * Creates a new Function call, which is basically an instance of the
	 * function's class.
	 * @param model  the Model to create the function call in
	 * @param function  the function class (must be a URI resource)
	 * @return a new instance of function
	 */
	public static FunctionCall createFunctionCall(Model model, Resource function) {
		return model.createResource(function).as(FunctionCall.class);
	}
	
	
	/**
	 * Creates a new Minus as a blank node in a given Model.
	 * @param model  the Model to create the MINUS in
	 * @param elements  the elements of the MINUS
	 * @return a new Minus
	 */
	public static Minus createMinus(Model model, ElementList elements) {
		Minus minus = model.createResource(SP.Minus).as(Minus.class);
		minus.addProperty(SP.elements, elements);
		return minus;
	}


	/**
	 * Creates a new NamedGraph element as a blank node in a given Model.
	 * @param model  the Model to generate the NamedGraph in
	 * @param graphNameNode  the URI resource of the graph name
	 * @param elements  the elements in the NamedGraph
	 * @return a new NamedGraph
	 */
	public static NamedGraph createNamedGraph(Model model, Resource graphNameNode, RDFList elements) {
		NamedGraph result = model.createResource(SP.NamedGraph).as(NamedGraph.class);
		result.addProperty(SP.graphNameNode, graphNameNode);
		result.addProperty(SP.elements, elements);
		return result;
	}
	
	
	/**
	 * Creates a new NotExists as a blank node in a given Model.
	 * @param model  the Model to create the NOT EXISTS in
	 * @param elements  the elements of the NOT EXISTS
	 * @return a new NotExists
	 */
	public static NotExists createNotExists(Model model, ElementList elements) {
		NotExists notExists = model.createResource(SP.NotExists).as(NotExists.class);
		notExists.addProperty(SP.elements, elements);
		return notExists;
	}
	

	/**
	 * Creates a new Optional as a blank node in a given Model. 
	 * @param model  the Model to create the OPTIONAL in
	 * @param elements  the elements of the OPTIONAL
	 * @return a new Optional
	 */
	public static Optional createOptional(Model model, ElementList elements) {
		Optional optional = model.createResource(SP.Optional).as(Optional.class);
		optional.addProperty(SP.elements, elements);
		return optional;
	}
	
	
	public static Service createService(Model model, Resource serviceURI, ElementList elements) {
		Service service = model.createResource(SP.Service).as(Service.class);
		service.addProperty(SP.serviceURI, serviceURI);
		service.addProperty(SP.elements, elements);
		return service;
	}
	

	/**
	 * Creates a new SubQuery as a blank node in a given Model.
	 * @param model  the Model to create the SubQuery in
	 * @param subQuery  the nested query
	 * @return a new SubQuery
	 */
	public static SubQuery createSubQuery(Model model, Query subQuery) {
		SubQuery result = model.createResource(SP.SubQuery).as(SubQuery.class);
		result.addProperty(SP.query, subQuery);
		return result;
	}
	
	
	/**
	 * Creates a new TemplateCall as a blank node instance of a given template.
	 * @param model  the Model to create a template call in
	 * @param template  the template class
	 * @return the new TemplateCall or null
	 */
	public static TemplateCall createTemplateCall(Model model, Resource template) {
		TemplateCall templateCall = model.createResource(template).as(TemplateCall.class);
		return templateCall;
	}


	/**
	 * Creates a new TriplePath as a blank node in a given Model.
	 * @param model  the Model to create the path in
	 * @param subject  the subject (not null)
	 * @param path  the path (not null)
	 * @param object  the object (not null)
	 * @return a new TriplePath
	 */
	public static TriplePath createTriplePath(Model model, Resource subject, Resource path, RDFNode object) {
		TriplePath triplePath = model.createResource(SP.TriplePath).as(TriplePath.class);
		triplePath.addProperty(SP.subject, subject);
		triplePath.addProperty(SP.path, path);
		triplePath.addProperty(SP.object, object);
		return triplePath;
	}


	/**
	 * Creates a new TriplePattern as a blank node in a given Model.
	 * @param model  the Model to create the pattern in
	 * @param subject  the subject (not null)
	 * @param predicate  the predicate (not null)
	 * @param object  the object (not null)
	 * @return a new TriplePattern
	 */
	public static TriplePattern createTriplePattern(Model model, Resource subject, Resource predicate, RDFNode object) {
		// No rdf:type sp:TriplePattern needed - engine looks for sp:predicate
		TriplePattern triplePattern = model.createResource().as(TriplePattern.class);
		triplePattern.addProperty(SP.subject, subject);
		triplePattern.addProperty(SP.predicate, predicate);
		triplePattern.addProperty(SP.object, object);
		return triplePattern;
	}
	

	/**
	 * Creates a new UNION element as a blank node in a given Model.
	 * @param model  the Model to create the Union in
	 * @param elements  the elements
	 * @return a new Union
	 */
	public static Union createUnion(Model model, ElementList elements) {
		Union union = model.createResource(SP.Union).as(Union.class);
		union.addProperty(SP.elements, elements);
		return union;
	}
	

	/**
	 * Creates a new Values element.
	 * @param model  the Model to create the Values in
	 * @param data  the Table providing the actual data
	 * @return a new Values
	 */
	public static Values createValues(Model model, Table data, boolean untyped) {
		
		Resource blank = untyped ? model.createResource() : model.createResource(SP.Values);
		Values values = blank.as(Values.class);
		
		List<RDFNode> vars = new ArrayList<RDFNode>(data.getVarNames().size());
		for(String varName : data.getVarNames()) {
			vars.add(model.createTypedLiteral(varName));
		}
		RDFList varList = model.createList(vars.iterator());
		values.addProperty(SP.varNames, varList);
		
		Iterator<Binding> bindings = data.rows();
		if(bindings.hasNext()) {
			List<RDFNode> lists = new LinkedList<RDFNode>();
			while(bindings.hasNext()) {
				List<RDFNode> nodes = new ArrayList<RDFNode>(data.getVarNames().size());
				Binding binding = bindings.next();
				for(String varName : data.getVarNames()) {
					Node value = binding.get(Var.alloc(varName));
					if(value == null) {
						nodes.add(SP.undef);
					}
					else {
						nodes.add(model.asRDFNode(value));
					}
				}
				lists.add(model.createList(nodes.iterator()));
			}
			values.addProperty(SP.bindings, model.createList(lists.iterator()));
		}
		
		return values;
	}
	
	
	/**
	 * Creates a new Variable as a blank node in a given Model.
	 * @param model  the Model
	 * @param varName  the name of the variable
	 * @return the Variable
	 */
	public static Variable createVariable(Model model, String varName) {
		Variable variable = model.createResource().as(Variable.class);
		variable.addProperty(SP.varName, model.createTypedLiteral(varName));
		return variable;
	}
	
	
	/**
	 * Gets an spl:Attribute defined for a given property on a given class.
	 * The spl:Attribute must be a direct spin:constraint on the class.
	 * @param cls  the class
	 * @param property  the property
	 * @return the Attribute or null if none is found
	 */
	public static Attribute getAttribute(Resource cls, Property property) {
		StmtIterator it = JenaUtil.listAllProperties(cls, SPIN.constraint);
		while(it.hasNext()) {
			RDFNode object = it.nextStatement().getObject();
			if(object instanceof Resource && ((Resource)object).hasProperty(RDF.type, SPL.Attribute)) {
				Attribute a = object.as(Attribute.class);
				if(property.equals(a.getPredicate())) {
					it.close();
					return a;
				}
			}
		}
		return null;
	}
	
	
	/**
	 * Gets the most appopriate metaclass to wrap a given Command into a
	 * Template.  For example, for an Ask query, this will return spin:AskTemplate.
	 * @param command  the Command, cast into the best possible subclass
	 * @return the Template metaclass
	 */
	public static Resource getTemplateMetaClass(Command command) {
		if(command instanceof Ask) {
			return SPIN.AskTemplate;
		}
		else if(command instanceof Construct) {
			return SPIN.ConstructTemplate;
		}
		else if(command instanceof Select) {
			return SPIN.SelectTemplate;
		}
		else if(command instanceof Update) {
			return SPIN.UpdateTemplate;
		}
		else {
			throw new IllegalArgumentException("Unsupported Command type: " + command.getClass());
		}
	}
	

	/**
	 * Checks whether a given module has been declared abstract using
	 * <code>spin:abstract</code.
	 * @param module  the module to test
	 * @return true if abstract
	 */
	public static boolean isAbstract(Resource module) {
		return module.hasProperty(SPIN.abstract_, module.getModel().createTypedLiteral(true));
	}


	/**
	 * Checks if a given Resource can be cast into an ElementList.
	 * It must be either rdf:nil or an rdf:List where the first
	 * list item is an element using <code>asElement()</code>.
	 * @param resource  the resource to test
	 * @return true if resource is an element list
	 */
	public static boolean isElementList(Resource resource) {
		if(RDF.nil.equals(resource)) {
			return true;
		}
		else {
			Statement firstS = resource.getProperty(RDF.first);
			if(firstS != null && firstS.getObject().isResource()) {
				Resource first = firstS.getResource();
				return asElement(first) != null;
			}
			else {
				return false;
			}
		}
	}
	
	
	/**
	 * Checks if a given Resource is an instance of a class that has
	 * type spin:Module (or its subclasses such as spin:Function).
	 * @param resource  the Resource to check
	 * @return true  if resource is a Module
	 */
	public static boolean isModuleInstance(Resource resource) {
		Resource moduleClass = SPIN.Module.inModel(resource.getModel());
		for(Resource type : JenaUtil.getTypes(resource)) {
			if(JenaUtil.hasIndirectType(type, moduleClass)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Checks if a given Property is spin:query or a sub-property of it.
	 * @param predicate  the Property to test
	 * @return true if predicate is a query property
	 */
	public static boolean isQueryProperty(Property predicate) {
		return SPIN.query.equals(predicate) || JenaUtil.hasSuperProperty(predicate, SPIN.query);
	}
	
	
	/**
	 * Checks whether a given RDFNode is a TemplateCall.  The condition for this
	 * is stricter than for <code>asTemplateCall</code> as the node also must have
	 * a valid template assigned to it, i.e. the type of the node must be an
	 * instance of spin:Template.
	 * @param node  the RDFNode to check
	 * @return true if node is a TemplateCall
	 */
	public static boolean isTemplateCall(RDFNode node) {
		TemplateCall templateCall = asTemplateCall(node);
		return templateCall != null && templateCall.getTemplate() != null;
	}
	
	
	/**
	 * Checks whether a given RDFNode is a variable.
	 * @param node  the node to check
	 * @return true if node is a variable
	 */
	public static boolean isVariable(RDFNode node) {
		return asVariable(node) != null;
	}
}
