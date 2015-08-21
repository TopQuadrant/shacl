/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.vocabulary;

import java.io.InputStream;

import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.spin.model.Aggregation;
import org.topbraid.spin.model.Argument;
import org.topbraid.spin.model.Ask;
import org.topbraid.spin.model.Attribute;
import org.topbraid.spin.model.Bind;
import org.topbraid.spin.model.Construct;
import org.topbraid.spin.model.Describe;
import org.topbraid.spin.model.Element;
import org.topbraid.spin.model.ElementList;
import org.topbraid.spin.model.Exists;
import org.topbraid.spin.model.Filter;
import org.topbraid.spin.model.Function;
import org.topbraid.spin.model.FunctionCall;
import org.topbraid.spin.model.Minus;
import org.topbraid.spin.model.Module;
import org.topbraid.spin.model.NamedGraph;
import org.topbraid.spin.model.NotExists;
import org.topbraid.spin.model.Optional;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.SPINInstance;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.model.Service;
import org.topbraid.spin.model.SubQuery;
import org.topbraid.spin.model.Template;
import org.topbraid.spin.model.TemplateCall;
import org.topbraid.spin.model.TriplePath;
import org.topbraid.spin.model.TriplePattern;
import org.topbraid.spin.model.TripleTemplate;
import org.topbraid.spin.model.Union;
import org.topbraid.spin.model.Values;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.model.impl.AggregationImpl;
import org.topbraid.spin.model.impl.ArgumentImpl;
import org.topbraid.spin.model.impl.AskImpl;
import org.topbraid.spin.model.impl.AttributeImpl;
import org.topbraid.spin.model.impl.BindImpl;
import org.topbraid.spin.model.impl.ConstructImpl;
import org.topbraid.spin.model.impl.DescribeImpl;
import org.topbraid.spin.model.impl.ElementListImpl;
import org.topbraid.spin.model.impl.ExistsImpl;
import org.topbraid.spin.model.impl.FilterImpl;
import org.topbraid.spin.model.impl.FunctionCallImpl;
import org.topbraid.spin.model.impl.FunctionImpl;
import org.topbraid.spin.model.impl.MinusImpl;
import org.topbraid.spin.model.impl.ModuleImpl;
import org.topbraid.spin.model.impl.NamedGraphImpl;
import org.topbraid.spin.model.impl.NotExistsImpl;
import org.topbraid.spin.model.impl.OptionalImpl;
import org.topbraid.spin.model.impl.SPINInstanceImpl;
import org.topbraid.spin.model.impl.SelectImpl;
import org.topbraid.spin.model.impl.ServiceImpl;
import org.topbraid.spin.model.impl.SubQueryImpl;
import org.topbraid.spin.model.impl.TemplateCallImpl;
import org.topbraid.spin.model.impl.TemplateImpl;
import org.topbraid.spin.model.impl.TriplePathImpl;
import org.topbraid.spin.model.impl.TriplePatternImpl;
import org.topbraid.spin.model.impl.TripleTemplateImpl;
import org.topbraid.spin.model.impl.UnionImpl;
import org.topbraid.spin.model.impl.ValuesImpl;
import org.topbraid.spin.model.impl.VariableImpl;
import org.topbraid.spin.model.update.Clear;
import org.topbraid.spin.model.update.Create;
import org.topbraid.spin.model.update.DeleteData;
import org.topbraid.spin.model.update.DeleteWhere;
import org.topbraid.spin.model.update.Drop;
import org.topbraid.spin.model.update.InsertData;
import org.topbraid.spin.model.update.Load;
import org.topbraid.spin.model.update.Modify;
import org.topbraid.spin.model.update.impl.ClearImpl;
import org.topbraid.spin.model.update.impl.CreateImpl;
import org.topbraid.spin.model.update.impl.DeleteDataImpl;
import org.topbraid.spin.model.update.impl.DeleteWhereImpl;
import org.topbraid.spin.model.update.impl.DropImpl;
import org.topbraid.spin.model.update.impl.InsertDataImpl;
import org.topbraid.spin.model.update.impl.LoadImpl;
import org.topbraid.spin.model.update.impl.ModifyImpl;
import org.topbraid.spin.util.SimpleImplementation;
import org.topbraid.spin.util.SimpleImplementation2;

import com.hp.hpl.jena.enhanced.BuiltinPersonalities;
import com.hp.hpl.jena.enhanced.Personality;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Vocabulary of the SPIN SPARQL Syntax schema.
 * 
 * @author Holger Knublauch
 */
public class SP {

    public final static String BASE_URI = "http://spinrdf.org/sp";

    public final static String NS = BASE_URI + "#";

    public final static String PREFIX = "sp";
    
    public final static String VAR_NS = "http://spinrdf.org/var#";

    public final static String VAR_PREFIX = "var";


    public final static Resource Aggregation = ResourceFactory.createResource(NS + "Aggregation");

    public final static Resource AltPath = ResourceFactory.createResource(NS + "AltPath");

    public final static Resource Asc = ResourceFactory.createResource(NS + "Asc");

    public final static Resource Ask = ResourceFactory.createResource(NS + "Ask");

    public final static Resource Avg = ResourceFactory.createResource(NS + "Avg");

    public final static Resource Bind = ResourceFactory.createResource(NS + "Bind");

    public final static Resource Clear = ResourceFactory.createResource(NS + "Clear");

    public final static Resource Command = ResourceFactory.createResource(NS + "Command");

    public final static Resource Construct = ResourceFactory.createResource(NS + "Construct");

    public final static Resource Count = ResourceFactory.createResource(NS + "Count");

    public final static Resource Create = ResourceFactory.createResource(NS + "Create");

    @Deprecated
    public final static Resource Delete = ResourceFactory.createResource(NS + "Delete");

    public final static Resource DeleteData = ResourceFactory.createResource(NS + "DeleteData");

    public final static Resource DeleteWhere = ResourceFactory.createResource(NS + "DeleteWhere");

    public final static Resource Desc = ResourceFactory.createResource(NS + "Desc");

    public final static Resource Describe = ResourceFactory.createResource(NS + "Describe");

    public final static Resource Drop = ResourceFactory.createResource(NS + "Drop");

    public final static Resource exists = ResourceFactory.createResource(NS + "exists");

    public final static Resource Exists = ResourceFactory.createResource(NS + "Exists");

    public final static Resource Expression = ResourceFactory.createResource(NS + "Expression");

    public final static Resource Filter = ResourceFactory.createResource(NS + "Filter");

    @Deprecated
    public final static Resource Insert = ResourceFactory.createResource(NS + "Insert");

    public final static Resource InsertData = ResourceFactory.createResource(NS + "InsertData");

    @Deprecated
    public final static Resource Let = ResourceFactory.createResource(NS + "Let");

    public final static Resource Load = ResourceFactory.createResource(NS + "Load");

    public final static Resource Max = ResourceFactory.createResource(NS + "Max");

    public final static Resource Min = ResourceFactory.createResource(NS + "Min");

    public final static Resource Modify = ResourceFactory.createResource(NS + "Modify");

    public final static Resource ModPath = ResourceFactory.createResource(NS + "ModPath");

    public final static Resource Minus = ResourceFactory.createResource(NS + "Minus");

    public final static Resource NamedGraph = ResourceFactory.createResource(NS + "NamedGraph");

    public final static Resource notExists = ResourceFactory.createResource(NS + "notExists");

    public final static Resource NotExists = ResourceFactory.createResource(NS + "NotExists");

    public final static Resource Optional = ResourceFactory.createResource(NS + "Optional");

    public final static Resource Query = ResourceFactory.createResource(NS + "Query");

    public final static Resource ReverseLinkPath = ResourceFactory.createResource(NS + "ReverseLinkPath");

    public final static Resource ReversePath = ResourceFactory.createResource(NS + "ReversePath");

    public final static Resource Select = ResourceFactory.createResource(NS + "Select");

    public final static Resource Service = ResourceFactory.createResource(NS + "Service");

    public final static Resource SeqPath = ResourceFactory.createResource(NS + "SeqPath");

    public final static Resource SubQuery = ResourceFactory.createResource(NS + "SubQuery");

    public final static Resource Sum = ResourceFactory.createResource(NS + "Sum");

    public final static Resource Triple = ResourceFactory.createResource(NS + "Triple");

    public final static Resource TriplePath = ResourceFactory.createResource(NS + "TriplePath");

    public final static Resource TriplePattern = ResourceFactory.createResource(NS + "TriplePattern");

    public final static Resource TripleTemplate = ResourceFactory.createResource(NS + "TripleTemplate");

    public final static Resource undef = ResourceFactory.createResource(NS + "undef");

    public final static Resource Union = ResourceFactory.createResource(NS + "Union");

    public final static Resource Update = ResourceFactory.createResource(NS + "Update");

    public final static Resource Values = ResourceFactory.createResource(NS + "Values");

    public final static Resource Variable = ResourceFactory.createResource(NS + "Variable");


	public final static Property all = ResourceFactory.createProperty(NS + "all");

	public final static Property arg = ResourceFactory.createProperty(NS + "arg");

	public final static Property arg1 = ResourceFactory.createProperty(NS + "arg1");

	public final static Property arg2 = ResourceFactory.createProperty(NS + "arg2");

	public final static Property arg3 = ResourceFactory.createProperty(NS + "arg3");

	public final static Property arg4 = ResourceFactory.createProperty(NS + "arg4");

	public final static Property arg5 = ResourceFactory.createProperty(NS + "arg5");
    
    public final static Property as = ResourceFactory.createProperty(NS + "as");
    
    public final static Property bindings = ResourceFactory.createProperty(NS + "bindings");

    public final static Property data = ResourceFactory.createProperty(NS + "data");

	public final static Property default_ = ResourceFactory.createProperty(NS + "default");
    
    public final static Property deletePattern = ResourceFactory.createProperty(NS + "deletePattern");
    
    public final static Property distinct = ResourceFactory.createProperty(NS + "distinct");
    
    public final static Property document = ResourceFactory.createProperty(NS + "document");
    
    public final static Property elements = ResourceFactory.createProperty(NS + "elements");
    
    public final static Property expression = ResourceFactory.createProperty(NS + "expression");
    
    public final static Property from = ResourceFactory.createProperty(NS + "from");
    
    public final static Property fromNamed = ResourceFactory.createProperty(NS + "fromNamed");

    public final static Property graphIRI = ResourceFactory.createProperty(NS + "graphIRI");
    
    public final static Property graphNameNode = ResourceFactory.createProperty(NS + "graphNameNode");
    
    public final static Property groupBy = ResourceFactory.createProperty(NS + "groupBy");
    
    public final static Property having = ResourceFactory.createProperty(NS + "having");
    
    public final static Property insertPattern = ResourceFactory.createProperty(NS + "insertPattern");
    
    public final static Property into = ResourceFactory.createProperty(NS + "into");
    
    public final static Property limit = ResourceFactory.createProperty(NS + "limit");
    
    public final static Property modMax = ResourceFactory.createProperty(NS + "modMax");
    
    public final static Property modMin = ResourceFactory.createProperty(NS + "modMin");

	public final static Property named = ResourceFactory.createProperty(NS + "named");
    
    public final static Property node = ResourceFactory.createProperty(NS + "node");
    
    public final static Property object = ResourceFactory.createProperty(NS + "object");
    
    public final static Property offset = ResourceFactory.createProperty(NS + "offset");
    
    public final static Property orderBy = ResourceFactory.createProperty(NS + "orderBy");
    
    public final static Property path = ResourceFactory.createProperty(NS + "path");
    
    public final static Property path1 = ResourceFactory.createProperty(NS + "path1");
    
    public final static Property path2 = ResourceFactory.createProperty(NS + "path2");

    public final static Property predicate = ResourceFactory.createProperty(NS + "predicate");
    
    public final static Property query = ResourceFactory.createProperty(NS + "query");

    public final static Property reduced = ResourceFactory.createProperty(NS + "reduced");

    public final static Property resultNodes = ResourceFactory.createProperty(NS + "resultNodes");

    public final static Property resultVariables = ResourceFactory.createProperty(NS + "resultVariables");
    
    public final static Property separator = ResourceFactory.createProperty(NS + "separator");
    
    public final static Property serviceURI = ResourceFactory.createProperty(NS + "serviceURI");
    
    public final static Property silent = ResourceFactory.createProperty(NS + "silent");

	public final static Property str = ResourceFactory.createProperty(NS + "str");

	public final static Property strlang = ResourceFactory.createProperty(NS + "strlang");

    public final static Property subject = ResourceFactory.createProperty(NS + "subject");

    public final static Property subPath = ResourceFactory.createProperty(NS + "subPath");

    public final static Property templates = ResourceFactory.createProperty(NS + "templates");

    public final static Property text = ResourceFactory.createProperty(NS + "text");
    
    public final static Property using = ResourceFactory.createProperty(NS + "using");
    
    public final static Property usingNamed = ResourceFactory.createProperty(NS + "usingNamed");
    
    public final static Property values = ResourceFactory.createProperty(NS + "values");

    public final static Property variable = ResourceFactory.createProperty(NS + "variable");
    
    public final static Property varName = ResourceFactory.createProperty(NS + "varName");
    
    public final static Property varNames = ResourceFactory.createProperty(NS + "varNames");
    
    public final static Property where = ResourceFactory.createProperty(NS + "where");
    
    public final static Property with = ResourceFactory.createProperty(NS + "with");
    
    
    public final static Resource bound = ResourceFactory.createResource(NS + "bound");
    
    public final static Resource eq = ResourceFactory.createResource(NS + "eq");
    
    public final static Resource not = ResourceFactory.createResource(NS + "not");

	public final static Resource regex = ResourceFactory.createResource(NS + "regex");

	public final static Resource sub = ResourceFactory.createResource(NS + "sub");

	public final static Resource unaryMinus = ResourceFactory.createResource(NS + "unaryMinus");
	
	
	private static Model model;
	

	/**
	 * Gets a Model with the content of the SP namespace, from a file
	 * that is bundled with this API.
	 * @return the namespace Model
	 */
	public static synchronized Model getModel() {
		if(model == null) {
			model = ModelFactory.createDefaultModel();
			InputStream is = SP.class.getResourceAsStream("/etc/sp.ttl");
			if(is == null) {
				model.read(SP.BASE_URI);
			}
			else {
				model.read(is, "http://dummy", FileUtils.langTurtle);
			}
		}
		return model;
	}

    
    static {
		SP.init(BuiltinPersonalities.model);
    }

    
    @SuppressWarnings("deprecation")
	private static void init(Personality<RDFNode> p) {
    	p.add(Aggregation.class, new SimpleImplementation(SPL.Argument.asNode(), AggregationImpl.class));
    	p.add(Argument.class, new SimpleImplementation(SPL.Argument.asNode(), ArgumentImpl.class));
    	p.add(Attribute.class, new SimpleImplementation(SPL.Attribute.asNode(), AttributeImpl.class));
    	p.add(Ask.class, new SimpleImplementation(Ask.asNode(), AskImpl.class));
    	p.add(Bind.class, new SimpleImplementation2(Bind.asNode(), Let.asNode(), BindImpl.class));
    	p.add(Clear.class, new SimpleImplementation(Clear.asNode(), ClearImpl.class));
    	p.add(Construct.class, new SimpleImplementation(Construct.asNode(), ConstructImpl.class));
    	p.add(Create.class, new SimpleImplementation(Create.asNode(), CreateImpl.class));
    	p.add(org.topbraid.spin.model.update.Delete.class, new SimpleImplementation(Delete.asNode(), org.topbraid.spin.model.update.impl.DeleteImpl.class));
    	p.add(DeleteData.class, new SimpleImplementation(DeleteData.asNode(), DeleteDataImpl.class));
    	p.add(DeleteWhere.class, new SimpleImplementation(DeleteWhere.asNode(), DeleteWhereImpl.class));
    	p.add(Describe.class, new SimpleImplementation(Describe.asNode(), DescribeImpl.class));
    	p.add(Drop.class, new SimpleImplementation(Drop.asNode(), DropImpl.class));
    	p.add(ElementList.class, new SimpleImplementation(RDF.List.asNode(), ElementListImpl.class));
    	p.add(Exists.class, new SimpleImplementation(Exists.asNode(), ExistsImpl.class));
    	p.add(Function.class, new SimpleImplementation(SPIN.Function.asNode(), FunctionImpl.class));
    	p.add(FunctionCall.class, new SimpleImplementation(SPIN.Function.asNode(), FunctionCallImpl.class));
    	p.add(Filter.class, new SimpleImplementation(Filter.asNode(), FilterImpl.class));
    	p.add(org.topbraid.spin.model.update.Insert.class, new SimpleImplementation(Insert.asNode(), org.topbraid.spin.model.update.impl.InsertImpl.class));
    	p.add(InsertData.class, new SimpleImplementation(InsertData.asNode(), InsertDataImpl.class));
    	p.add(Load.class, new SimpleImplementation(Load.asNode(), LoadImpl.class));
    	p.add(Minus.class, new SimpleImplementation(Minus.asNode(), MinusImpl.class));
    	p.add(Modify.class, new SimpleImplementation(Modify.asNode(), ModifyImpl.class));
    	p.add(Module.class, new SimpleImplementation(SPIN.Module.asNode(), ModuleImpl.class));
    	p.add(NamedGraph.class, new SimpleImplementation(NamedGraph.asNode(), NamedGraphImpl.class));
    	p.add(NotExists.class, new SimpleImplementation(NotExists.asNode(), NotExistsImpl.class));
    	p.add(Optional.class, new SimpleImplementation(Optional.asNode(), OptionalImpl.class));
    	p.add(Service.class, new SimpleImplementation(Service.asNode(), ServiceImpl.class));
    	p.add(Select.class, new SimpleImplementation(Select.asNode(), SelectImpl.class));
    	p.add(SubQuery.class, new SimpleImplementation(SubQuery.asNode(), SubQueryImpl.class));
    	p.add(SPINInstance.class, new SimpleImplementation(RDFS.Resource.asNode(), SPINInstanceImpl.class));
    	p.add(Template.class, new SimpleImplementation(SPIN.Template.asNode(), TemplateImpl.class));
    	p.add(TemplateCall.class, new SimpleImplementation(RDFS.Resource.asNode(), TemplateCallImpl.class));
    	p.add(TriplePath.class, new SimpleImplementation(TriplePath.asNode(), TriplePathImpl.class));
    	p.add(TriplePattern.class, new SimpleImplementation(TriplePattern.asNode(), TriplePatternImpl.class));
    	p.add(TripleTemplate.class, new SimpleImplementation(TripleTemplate.asNode(), TripleTemplateImpl.class));
    	p.add(Union.class, new SimpleImplementation(Union.asNode(), UnionImpl.class));
    	p.add(Values.class, new SimpleImplementation(Values.asNode(), ValuesImpl.class));
    	p.add(Variable.class, new SimpleImplementation(Variable.asNode(), VariableImpl.class));
    	
    	// Also make sure SHACL is started up
    	new SHACLFactory();
    }
    
    
    /**
     * Checks whether the SP ontology is used in a given Model.
     * This is true if the model defines the SP namespace prefix
     * and also has sp:Query defined with an rdf:type.
     * The goal of this call is to be very fast when SP is not
     * imported, i.e. it checks the namespace first and can then
     * omit the type query.
     * @param model  the Model to check
     * @return true if SP exists in model
     */
    public static boolean exists(Model model) {
    	return model != null &&
    		SP.NS.equals(model.getNsPrefixURI(SP.PREFIX)) && 
    		model.contains(SP.Query, RDF.type, (RDFNode)null);
    }
    
    
    public static Property getArgProperty(int index) {
    	return ResourceFactory.createProperty(NS + "arg" + index);
    }
    
    
    public static Property getArgProperty(String varName) {
    	return ResourceFactory.createProperty(NS + varName);
    }
    
    
    public static Integer getArgPropertyIndex(String varName) {
    	if(varName.startsWith("arg")) {
    		String subString = varName.substring(3);
    		try {
    			return Integer.getInteger(subString);
    		}
    		catch(Throwable t) {
    		}
    	}
    	return null;
    }
	
	
	public static String getURI() {
        return NS;
    }


	public static void toStringElementList(StringBuffer buffer, Resource resource) {
		RDFList list = resource.as(RDFList.class);
		for(ExtendedIterator<RDFNode> it = list.iterator(); it.hasNext(); ) {
			Resource item = (Resource) it.next();
			Element e = SPINFactory.asElement(item);
			buffer.append(e.toString());
			if(it.hasNext()) {
				buffer.append(" .\n");
			}
		}
	}
}
