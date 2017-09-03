/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.arq;

import java.util.*;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.engine.iterator.QueryIterConcat;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.iterator.QueryIteratorWrapper;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;
import org.apache.jena.sparql.util.IterLib;
import org.topbraid.spin.model.Argument;
import org.topbraid.spin.model.Function;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.system.MagicPropertyPolicy;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.vocabulary.SPIN;

/**
 * An ARQ PropertyFunction based on a spin:MagicProperty.
 * For convenience, also implements PropertyFunctionFactory.
 *
 * @author Holger Knublauch
 */
public class SPINARQPFunction extends PropertyFunctionBase implements PropertyFunctionFactory {
	
	public static final String SELECT_STAR_NOT_SUPPORTED_IN_MAGIC_PROPERTIES = "SELECT * not supported in magic properties";

	public static final String SELECT_WITH_EXPRESSIONS_NOT_SUPPORTED_IN_MAGIC_PROPERTIES = "SELECT with expressions not supported in magic properties";

	private org.apache.jena.query.Query arqQuery;

	private org.apache.jena.query.Query arqInverseQuery;

	private String queryString;
	
	private List<String> objectVarNames = new ArrayList<String>();

	
	public SPINARQPFunction(Function functionCls) {
		
		Select spinQuery = (Select) functionCls.getBody();
		queryString = ARQFactory.get().createCommandString(spinQuery);
		
		List<String> resultVariables = spinQuery.getResultVariableNames();
		if(resultVariables.isEmpty()) {
			throw new IllegalArgumentException(SELECT_STAR_NOT_SUPPORTED_IN_MAGIC_PROPERTIES);
		}
		for(String varName : resultVariables) {
			if(varName != null) {
				objectVarNames.add(varName);
			}
			else {
				throw new IllegalArgumentException(SELECT_WITH_EXPRESSIONS_NOT_SUPPORTED_IN_MAGIC_PROPERTIES);
			}
		}
		int selectStart = queryString.indexOf("SELECT ");
		int eol = queryString.indexOf('\n', selectStart);
		
		StringBuffer sb = new StringBuffer(queryString.substring(0, eol));
		List<Argument> arguments = functionCls.getArguments(true);
		for(Argument arg : arguments) {
			sb.append(" ?");
			sb.append(arg.getVarName());
		}
		sb.append(queryString.substring(eol));
			
		try {
			arqQuery = ARQFactory.get().createQuery(sb.toString());
		}
		catch(Exception ex) {
			throw new IllegalArgumentException("Magic property " + functionCls + " does not contain a valid body. Internally used query string:\n" + sb, ex);
		}
		
		Resource spinInverseQuery = functionCls.getPropertyResourceValue(SPIN.inverseBody);
		if(spinInverseQuery != null) {
			String inverseQueryString = ARQFactory.get().createCommandString(SPINFactory.asCommand(spinInverseQuery));
			int inverseSelectStart = inverseQueryString.indexOf("SELECT ");
			int inverseEol = inverseQueryString.indexOf('\n', inverseSelectStart);
			StringBuffer isb = new StringBuffer(inverseQueryString.substring(0, inverseEol));
			isb.append(" ?" + SPIN.INVERSE_OBJECT_VAR_NAME + " ");
			isb.append(inverseQueryString.substring(inverseEol));
			try {
				arqInverseQuery = ARQFactory.get().createQuery(isb.toString());
			}
			catch(Exception ex) {
				throw new IllegalArgumentException("Magic property " + functionCls + " does not contain a valid inverse body. Internally used query string:\n" + isb, ex);
			}
		}
	}

	
	@Override
    public PropertyFunction create(String arg0) {
		return this;
	}

	
    @Override
	public QueryIterator exec(Binding inputBinding, PropFuncArg argSubject, Node predicate,
			PropFuncArg argObject, ExecutionContext context) {

		argObject = Substitute.substitute(argObject, inputBinding);
		argSubject = Substitute.substitute(argSubject, inputBinding);
		
		ExprList subjectExprList = argSubject.asExprList();
		ExprList objectExprList = argObject.asExprList();
		
		QueryIterConcat existingValues = null;
		MagicPropertyPolicy.Policy policy = MagicPropertyPolicy.Policy.QUERY_RESULTS_ONLY;
		Query theQuery = arqQuery;
		
		// Use spin:inverseBody if the (only) variable on the right is bound
		if(arqInverseQuery != null && objectExprList.size() == 1) {
			if(objectExprList.get(0).isConstant()) {
				theQuery = arqInverseQuery;
			}
		}
		
		// Handle cases with one argument on both sides (S, P, O)
		if(objectExprList.size() == 1 && subjectExprList.size() == 1) {
			Expr subject = subjectExprList.get(0);
			Expr object = objectExprList.get(0);

			if(subject.isVariable() || object.isVariable()) {
				
				Node matchSubject = null;
				if(subject.isConstant()) {
					Node n = subject.getConstant().asNode();
					if(n.isURI() || n.isBlank()) {
						matchSubject = n;
					}
				}
				
				Node matchObject = null;
				if(object.isConstant()) {
					matchObject = object.getConstant().asNode();
				}
				
				Graph queryGraph = context.getActiveGraph();
				policy = MagicPropertyPolicy.get().getPolicy(predicate.getURI(), queryGraph, matchSubject, matchObject);

				if(policy != MagicPropertyPolicy.Policy.QUERY_RESULTS_ONLY) {
					Iterator<Triple> it = queryGraph.find(matchSubject, predicate, matchObject);
					while(it.hasNext()) {
						Triple triple = it.next();
						BindingMap map = new BindingHashMap(inputBinding);
						if(subject.isVariable()) {
							map.add(subject.asVar(), triple.getSubject());
						}
						if(object.isVariable()) {
							map.add(object.asVar(), triple.getObject());
						}
						if(existingValues == null) {
							existingValues = new QueryIterConcat(context);
						}
						QueryIterator nested = IterLib.result(map, context);
						existingValues.add(nested);
					}
				}
			}
		}
		
		if(policy != MagicPropertyPolicy.Policy.TRIPLES_ONLY) {
			
			Graph activeGraph = context.getActiveGraph();
			if(activeGraph == null) {
				activeGraph = JenaUtil.createDefaultGraph();
			}
			Model model = ModelFactory.createModelForGraph(activeGraph);
			Node t = inputBinding.get(Var.alloc(SPIN.THIS_VAR_NAME));
			QuerySolutionMap bindings = new QuerySolutionMap();
			if(t != null) {
				bindings.add(SPIN.THIS_VAR_NAME, model.asRDFNode(t));
			}
	
			// Map object expressions to original objectVarNames
			Map<String,Var> vars = new HashMap<String,Var>();
			if(theQuery == arqInverseQuery) {
	        	NodeValue x = objectExprList.get(0).getConstant();
        		bindings.add(SPIN.INVERSE_OBJECT_VAR_NAME, model.asRDFNode(x.asNode()));
			}
			else {
				for(int i = 0; i < objectVarNames.size() && i < objectExprList.size(); i++) {
					Expr expr = objectExprList.get(i);
					String objectVarName = objectVarNames.get(i);
					if(expr.isVariable() && !inputBinding.contains(expr.asVar())) {
						Var var = expr.asVar();
						vars.put(objectVarName, var);
					}
					else {
			        	NodeValue x = expr.eval(inputBinding, context);
			        	if(x != null) {
			        		bindings.add(objectVarName, model.asRDFNode(x.asNode()));
			        	}
					}
				}
			}
			
			// Map subject expressions to arg1 etc
			for(int i = 0; i < subjectExprList.size(); i++) {
				String subjectVarName = "arg" + (i + 1);
				Expr expr = subjectExprList.get(i);
				if(expr.isVariable() && !inputBinding.contains(expr.asVar())) {
					Var var = expr.asVar();
					vars.put(subjectVarName, var);
				}
				else {
		        	NodeValue x = expr.eval(inputBinding, context);
		        	if(x != null) {
		        		bindings.add(subjectVarName, model.asRDFNode(x.asNode()));
		        	}
				}
			}
			
            // Query Dataset or model
            Dataset ds = (context.getDataset() != null) 
                ? new DatasetWithDifferentDefaultModel(model, DatasetImpl.wrap(context.getDataset()))
                : null;

            // Suppress "resource" - qexec is passed out of scope because queryIterator
            // (QueryIterator it) is returned from this property function.
            @SuppressWarnings("all")
            QueryExecution qexec = (ds != null) ? ARQFactory.get().createQueryExecution(theQuery, ds, bindings)
                                                : ARQFactory.get().createQueryExecution(theQuery, model);
            ResultSet rs = qexec.execSelect();
            List<Var> rvs = theQuery.getProjectVars();
            // QuerySolution to result Binding 
            Iterator<Binding> x = Iter.map(rs, qs->convertRow(qs, vars, rvs, inputBinding));
            // Become a QueryIterator
            QueryIterator it0 = new QueryIterPlainWrapper(x, context);
            // Add saftey wrapper
            QueryIterator it  = QueryIteratorClosing.protect(it0, qexec);
            if(existingValues != null) {
                existingValues.add(it);
                return existingValues;
            }
            else {
                return it;
            }
            
            // Do by materialization. This is safer. 
            // It detaches the iterator from the inner QueryExecution, which is then closed.
            // The results are then solely part of the queye execution that called exec. 
            
//          try ( QueryExecution qexec = (ds != null) ? ARQFactory.get().createQueryExecution(theQuery, ds, bindings)
//                                                    : ARQFactory.get().createQueryExecution(theQuery, model) ) {
//              ResultSet rs = qexec.execSelect();
//              List<Var> rvs = theQuery.getProjectVars();
//              QueryIterator it = new QueryIterPlainWrapper(rsBindings.iterator(), context);
//              // QuerySolution to result Binding 
//              List<Binding> bx = Iter.iter(rs).map(qs->convertRow(qs, vars, rvs, inputBinding)).toList();
//              QueryIterator it = new QueryIterPlainWrapper(bx.iterator, context);
//              if(existingValues != null) {
//                  existingValues.add(it);
//                  return existingValues;
//              }
//              else {
//                  return it;
//              }
//          }
		}
		else if(existingValues != null) {
			return existingValues;
		}
		else {
			return IterLib.result(inputBinding, context);
		}
	}
    
    private static Binding convertRow(QuerySolution row, Map<String, Var> vars, List<Var> rvs, Binding parentBinding) {
        BindingMap result = BindingFactory.create(parentBinding);
        for(Var var1 : rvs) {
            RDFNode resultNode = row.get(var1.getVarName());
            if(resultNode != null) {
                Var var2 = vars.get(var1.getVarName());
                if(var2 != null) {
                    result.add(var2, resultNode.asNode());
                }
            }
        }
        return result;
    }
    
    // If anything goes wrong during the iteration from the property function, which is not an ARQ exception,
    // then close the AutoCloseable which is a QueryExecution.
    //
    // QueryIteratorClosing adds try-catch around points where the property function
    // can be involved, and close and cancel cause the closeable to be closed.
    // Assumes the QueryIterator is used properly.
    private static class QueryIteratorClosing extends QueryIteratorWrapper {

        static QueryIterator protect(QueryIterator qIter, AutoCloseable closeable) {
            if ( qIter instanceof QueryIteratorClosing )
                Log.warn(SPINARQFunction.class, "Wrapping an already wrapped QueryIteratorClosing");
            return new QueryIteratorClosing(qIter, closeable);
        }
        
        private final AutoCloseable closeable;

        private QueryIteratorClosing(QueryIterator qIter, AutoCloseable closeable) {
            super(qIter);
            this.closeable = closeable;
        }
        
        @Override
        protected boolean hasNextBinding() {
            try {
                return super.hasNextBinding() ;
            }
            catch (RuntimeException ex) { closeInternal() ; throw ex; }
        }
        
        @Override
        protected Binding moveToNextBinding() {
            try {
                return super.moveToNextBinding();
            }
            catch (RuntimeException ex) { closeInternal() ; throw ex; }
        }

        @Override
        protected void closeIterator() {
            closeInternal();
            super.closeIterator();
        }
        
        @Override
        protected void requestCancel() {
            closeInternal();
            super.requestCancel();
        }
        
        private void closeInternal() {
            try {
                closeable.close();
            } catch (RuntimeException ex) { 
                throw ex;
            } catch (Exception ex) {
                throw new IllegalStateException("Unexpected checked exception", ex);
            }
        }
    }
}
