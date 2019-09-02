package org.topbraid.jenax.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.syntax.ElementAssign;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementVisitor;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.sparql.util.VarUtils;


public class VarFinder {
	
	/**
	 * Gets the names of all variables mentioned in a SPARQL Query.
	 * @param query  the Query to query
	 * @return a Set of variables
	 */
	public static Set<String> varsMentioned(Query query) {
		final Set<String> results = new HashSet<>();

		if(query.isSelectType()) {
			for(Object var : query.getResultVars()) {
				results.add((String)var);
			}
		}
		else if(query.isConstructType()) {
			for(Triple t : query.getConstructTemplate().getTriples()) {
				if(t.getMatchSubject().isVariable()) {
					results.add(t.getMatchSubject().getName());
				}
				if(t.getMatchPredicate().isVariable()) {
					results.add(t.getMatchPredicate().getName());
				}
				if(t.getMatchObject().isVariable()) {
					results.add(t.getMatchObject().getName());
				}
			}
		}

		final Set<Var> vars = new HashSet<Var>();
        ElementVisitor v = new ElementVisitorBase() {
        	
			@Override
            public void visit(ElementTriplesBlock el) {
                for (Iterator<Triple> iter = el.patternElts(); iter.hasNext(); ) {
                    Triple t = iter.next() ;
                    VarUtils.addVarsFromTriple(vars, t) ;
                }
            }

			@Override
            public void visit(ElementPathBlock el) {
                for (Iterator<TriplePath> iter = el.patternElts() ; iter.hasNext() ; ) {
                    TriplePath tp = iter.next() ;
                    // If it's triple-izable, then use the triple. 
                    if ( tp.isTriple() ) {
                        VarUtils.addVarsFromTriple(vars, tp.asTriple()) ;
                    }
                    else {
                        VarUtils.addVarsFromTriplePath(vars, tp) ;
                    }
                }
            }
            
            @Override
            public void visit(ElementFilter el) {
        	  	ExprVars.varsMentioned(vars, el.getExpr());
          	}

            @Override
            public void visit(ElementNamedGraph el) {
                VarUtils.addVar(vars, el.getGraphNameNode()) ;
            }
            
            @Override
            public void visit(ElementService el) {
            	VarUtils.addVar(vars, el.getServiceNode());
            }
            
			@Override
            public void visit(ElementSubQuery el) {
                el.getQuery().setResultVars() ;
                VarExprList x = el.getQuery().getProject() ;
                vars.addAll(x.getVars()) ;
            }
            
            @Override
            public void visit(ElementAssign el) {
                vars.add(el.getVar()) ;
        	  	ExprVars.varsMentioned(vars, el.getExpr());
            }
        };   	
        ElementWalker.walk(query.getQueryPattern(), v) ;
        
        for(Var var : vars) {
        	results.add(var.getName());
        }
        
        return results;
    }
}
