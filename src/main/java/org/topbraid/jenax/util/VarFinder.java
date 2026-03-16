package org.topbraid.jenax.util;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.syntax.*;
import org.apache.jena.sparql.util.VarUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class VarFinder {

    /**
     * Gets the names of all variables mentioned in a SPARQL Query.
     *
     * @param query the Query to query
     * @return a Set of variables
     */
    public static Set<String> varsMentioned(Query query) {
        final Set<String> results = new HashSet<>();

        if (query.isSelectType()) {
            for (Object var : query.getResultVars()) {
                results.add((String) var);
            }
        } else if (query.isConstructType()) {
            for (Triple t : query.getConstructTemplate().getTriples()) {
                if (t.getSubject().isVariable()) {
                    results.add(t.getSubject().getName());
                }
                if (t.getPredicate().isVariable()) {
                    results.add(t.getPredicate().getName());
                }
                if (t.getObject().isVariable()) {
                    results.add(t.getObject().getName());
                }
            }
        }

        final Set<Var> vars = new HashSet<Var>();
        ElementVisitor v = new ElementVisitorBase() {

            @Override
            public void visit(ElementTriplesBlock el) {
                for (Iterator<Triple> iter = el.patternElts(); iter.hasNext(); ) {
                    Triple t = iter.next();
                    VarUtils.addVarsFromTriple(vars, t);
                }
            }

            @Override
            public void visit(ElementPathBlock el) {
                for (Iterator<TriplePath> iter = el.patternElts(); iter.hasNext(); ) {
                    TriplePath tp = iter.next();
                    // If it's triple-izable, then use the triple.
                    if (tp.isTriple()) {
                        VarUtils.addVarsFromTriple(vars, tp.asTriple());
                    } else {
                        VarUtils.addVarsFromTriplePath(vars, tp);
                    }
                }
            }

            @Override
            public void visit(ElementFilter el) {
                ExprVars.varsMentioned(vars, el.getExpr());
            }

            @Override
            public void visit(ElementNamedGraph el) {
                VarUtils.addVar(vars, el.getGraphNameNode());
            }

            @Override
            public void visit(ElementService el) {
                VarUtils.addVar(vars, el.getServiceNode());
            }

            @Override
            public void visit(ElementSubQuery el) {
                el.getQuery().ensureResultVars();
                VarExprList x = el.getQuery().getProject();
                vars.addAll(x.getVars());
            }

            @Override
            public void visit(ElementAssign el) {
                vars.add(el.getVar());
                ExprVars.varsMentioned(vars, el.getExpr());
            }
        };
        ElementWalker.walk(query.getQueryPattern(), v);

        for (Var var : vars) {
            results.add(var.getName());
        }

        return results;
    }
}
