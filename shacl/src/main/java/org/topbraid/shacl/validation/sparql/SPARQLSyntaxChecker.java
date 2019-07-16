/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */
package org.topbraid.shacl.validation.sparql;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprFunctionOp;
import org.apache.jena.sparql.expr.ExprNone;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.ExprVisitorFunction;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementVisitor;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.RecursiveElementVisitor;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Can be used to check for the violation of any of the syntax rules in Appendix A
 * of the SHACL spec, to prevent certain pre-binding scenarios.
 * 
 * @author Holger Knublauch
 */
public class SPARQLSyntaxChecker {

	/**
	 * Checks whether a given Query violates any of the syntax rules in Appendix A.
	 * @param query  the Query to check
	 * @param preBoundVars  the potentially pre-bound variables
	 * @return an List of error messages (empty if OK)
	 */
	public static List<String> checkQuery(Query query, Set<String> preBoundVars) {
		List<String> results = new LinkedList<>();
		ElementVisitor elementVisitor = new RecursiveElementVisitor(new ElementVisitorBase()) {

			@Override
			public void startElement(ElementBind el) {
				if(preBoundVars.contains(el.getVar().getVarName())) {
					if(SH.valueVar.getVarName().equals(el.getVar().getVarName()) && el.getExpr().isVariable() && el.getExpr().asVar().equals(SH.thisVar)) {
						// Ignore clauses injected by engine
					}
					else {
						results.add("Query must not reassign the pre-bound variable " + el.getVar() + " in a BIND clause");
					}
				}
			}

			@Override
			public void startElement(ElementData el) {
				results.add("Query must not contain VALUES clause");
			}

			@Override
			public void startElement(ElementFilter el) {
				checkExpression(el.getExpr());
			}

			@Override
			public void startElement(ElementMinus el) {
				results.add("Query must not contain MINUS clause");
			}

			@Override
			public void startElement(ElementService el) {
				results.add("Query must not contain SERVICE clause");
			}

			@Override
			public void startElement(ElementSubQuery el) {
				if(el.getQuery().isQueryResultStar()) {
					Set<Var> queryVars = new LinkedHashSet<>() ;
					PatternVars.vars(queryVars, el.getQuery().getQueryPattern()) ;
					for(String varName : preBoundVars) {
						if(!SH.currentShapeVar.getVarName().equals(varName) && !SH.shapesGraphVar.getVarName().equals(varName)) {
							if(!queryVars.contains(Var.alloc(varName))) {
								results.add("Sub-query must return all potentially pre-bound variables including $" + varName);
							}
						}
					}
				}
				else {
					VarExprList project = el.getQuery().getProject();
					for(String varName : preBoundVars) {
						if(!SH.currentShapeVar.getVarName().equals(varName) && !SH.shapesGraphVar.getVarName().equals(varName)) {
							if(!project.contains(Var.alloc(varName))) {
								results.add("Sub-query must return all potentially pre-bound variables including $" + varName);
							}
						}
					}
				}
			}
			
			private void checkExpression(Expr expr) {
				final ElementVisitor parent = this;
				expr.visit(new ExprVisitorFunction() {
					@Override
					public void visit(ExprFunctionOp funcOp) {
						if(funcOp.isGraphPattern()) {
							funcOp.getElement().visit(parent);
						}
					}
					@Override
					public void visit(NodeValue nv) {
					}
					@Override
					public void visit(ExprVar nv) {
					}
					@Override
					public void visit(ExprAggregator eAgg) {
					}
					@Override
					public void visit(ExprNone exprNone) {
					}
					@Override
					protected void visitExprFunction(ExprFunction func) {
						for(Expr expr : func.getArgs()) {
							expr.visit(this);
						}
					}
				});
			}
		};
		query.getQueryPattern().visit(elementVisitor);
		return results;
	}
}
