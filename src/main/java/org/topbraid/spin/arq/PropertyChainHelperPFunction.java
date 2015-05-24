package org.topbraid.spin.arq;

import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterConcat;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionBase;
import com.hp.hpl.jena.sparql.util.IterLib;

/**
 * A helper property function needed by OWL 2 RL rule prp-spo2.
 * This rule needs to walk rdf:Lists of arbitrary length and
 * match triple along the way - very hard to express in pure SPARQL.
 * 
 * @author Holger Knublauch
 */
public class PropertyChainHelperPFunction extends PropertyFunctionBase {

	@Override
	public QueryIterator exec(final Binding binding, PropFuncArg argSubject,
			Node predicate, PropFuncArg argObject, final ExecutionContext execCxt) {
		
		final QueryIterConcat concat = new QueryIterConcat(execCxt);
		
		argSubject = Substitute.substitute(argSubject, binding);
		argObject = Substitute.substitute(argObject, binding);
		Model model = ModelFactory.createModelForGraph(execCxt.getActiveGraph());
		RDFList rdfList = model.asRDFNode(argSubject.getArg()).as(RDFList.class);
		List<RDFNode> ps = rdfList.asJavaList();
		Property[] properties = new Property[ps.size()];
		for(int i = 0; i < ps.size(); i++) {
			properties[i] = ps.get(i).as(Property.class);
		}
		List<Node> objectList = argObject.getArgList();
		final Var subjectVar = (Var) objectList.get(0);
		final Var objectVar = (Var) objectList.get(1);
		
		if(ps.size() > 1) {
			StmtIterator it = model.listStatements(null, properties[0], (RDFNode)null);
			while(it.hasNext()) {
				Statement s = it.next();
				List<Node> tails = new LinkedList<Node>();
				if(s.getObject().isResource()) {
					addTails(properties, 1, s.getResource(), tails);
					for(Node tail : tails) {
						BindingMap map = new BindingHashMap(binding);
						map.add(subjectVar, s.getSubject().asNode());
						map.add(objectVar, tail);
						concat.add(IterLib.result(map, execCxt));
					}
				}
			}
		}
		
		return concat;
	}
	
	
	private void addTails(Property[] properties, int i, Resource subject, List<Node> results) {
		if(i == properties.length) {
			results.add(subject.asNode());
		}
		else {
			StmtIterator it = subject.listProperties(properties[i]);
			while(it.hasNext()) {
				Statement s = it.next();
				if(s.getObject().isResource()) {
					addTails(properties, i + 1, s.getResource(), results);
				}
			}
		}
	}
}
