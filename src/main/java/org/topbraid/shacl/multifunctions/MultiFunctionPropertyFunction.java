package org.topbraid.shacl.multifunctions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;

/**
 * A Jena property function (magic property) that wraps a MultiFunction instance.
 * 
 * @author Holger Knublauch
 */
class MultiFunctionPropertyFunction extends PropertyFunctionBase implements PropertyFunctionFactory {
	
	private MultiFunction multiFunction;
	
	
	MultiFunctionPropertyFunction(MultiFunction multiFunction) {
		this.multiFunction = multiFunction;
	}


	@Override
	public PropertyFunction create(String uri) {
		return this;
	}


	@Override
	public QueryIterator exec(Binding parentBinding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject,
			ExecutionContext execCxt) {
        
		argSubject = Substitute.substitute(argSubject, parentBinding);
		List<Node> args = args(argSubject);
		
		argObject = Substitute.substitute(argObject, parentBinding);
		Map<String, Var> vars = varsMap(argObject, predicate);
		
		QueryIterator quit = multiFunction.execute(args, execCxt.getActiveGraph(), execCxt.getDataset());
		Iterator<Binding> bindings = Iter.map(quit, binding -> convert(binding, vars, parentBinding));
		return QueryIterPlainWrapper.create(bindings);
	}


	private List<Node> args(PropFuncArg argSubject) {
		if(argSubject.isList()) {
			List<Node> results = new LinkedList<>();
			for(Node arg : argSubject.getArgList()) {
				if(arg.isVariable()) {
					results.add(null);
				}
				else {
					results.add(arg);
				}
			}
			return results;
		}
		else {
			Node arg = argSubject.getArg();
			if(arg.isVariable()) {
				return Collections.singletonList(null);
			}
			else {
				return Collections.singletonList(arg);
			}
		}
	}
    
	
    private Binding convert(Binding binding, Map<String, Var> varsMap, Binding parentBinding) {
		BindingBuilder builder = BindingBuilder.create(parentBinding);
		Iterator<Var> vars = binding.vars();
		while(vars.hasNext()) {
			Var var = vars.next();
			Node resultNode = binding.get(var);
			if(resultNode != null) {
				Var var2 = varsMap.get(var.getVarName());
				if(var2 != null) {
					builder.add(var2, resultNode);
				}
			}
		}
		return builder.build();
	}


	private Map<String,Var> varsMap(PropFuncArg argObject, Node predicate) {
		List<MultiFunctionParameter> vars = multiFunction.getResultVars();
		Map<String,Var> map = new HashMap<>();
		if(argObject.isList()) {
			List<Node> argList = argObject.getArgList();
			for(int i = 0; i < argList.size() && i < vars.size(); i++) {
				Node arg = argList.get(i);
				if(arg.isVariable()) {
					String varName = vars.get(i).getName();
					map.put(varName, Var.alloc(arg));
				}
				else {
					throw new ExprEvalException("Nodes on the right side of property function " + predicate + " must be unbound variables");
				}
			}
		}
		else {
			Node arg = argObject.getArg();
			if(arg.isVariable()) {
				String varName = vars.get(0).getName();
				map.put(varName, Var.alloc(arg));
			}
			else {
				throw new ExprEvalException("Nodes on the right side of property function " + predicate + " must be unbound variables");
			}
		}
		return map;
	}
}
