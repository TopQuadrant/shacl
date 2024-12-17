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

package org.topbraid.shacl.arq.functions;

import java.util.List;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.Context;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.DatasetWithDifferentDefaultModel;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.arq.SHACLARQFunction;
import org.topbraid.shacl.model.SHConstraintComponent;
import org.topbraid.shacl.model.SHSPARQLFunction;
import org.topbraid.shacl.validation.sparql.SPARQLSubstitutions;
import org.topbraid.shacl.vocabulary.SH;


/**
 * An ARQ function that is based on a sh:SPARQLFunction.
 * 
 * There are two ways of declaring such functions:
 * - as sh:Function (similar to SPIN functions)
 * - from constraint components that point at sh:SPARQLAskValidators
 * This class has two constructors for those two cases.
 * 
 * @author Holger Knublauch
 */
public class SHACLSPARQLARQFunction extends SHACLARQFunction {
	
	private org.apache.jena.query.Query arqQuery;
	
	private String queryString;

	@Override
	public void build(String uri, ExprList args, Context context) {
	}

	/**
	 * Constructs a new SHACLSPARQLARQFunction based on a given sh:ConstraintComponent
	 * and a given validator (which must be a value of sh:nodeValidator, sh:propertyValidator etc.
	 * @param component  the constraint component (defining the sh:parameters)
	 * @param askValidator  the sh:SPARQLAskValidator resource
	 */
	public SHACLSPARQLARQFunction(SHConstraintComponent component, Resource askValidator) {
		
		super(null);
		
		try {
			queryString = JenaUtil.getStringProperty(askValidator, SH.ask);
			arqQuery = ARQFactory.get().createQuery(SPARQLSubstitutions.withPrefixes(queryString, askValidator));
		}
		catch(Exception ex) {
			throw new IllegalArgumentException("Validator " + askValidator + " does not define a valid body", ex);
		}
		if(!arqQuery.isAskType()) {
            throw new ExprEvalException("Body must be ASK query");
		}
		
		paramNames.add("value");
		addParameters(component);
		paramNames.add("shapesGraph");
	}
	

	/**
	 * Constructs a new SHACLSPARQLARQFunction based on a given sh:Function.
	 * The shaclFunction must be associated with the Model containing
	 * the triples of its definition.
	 * @param shaclFunction  the SHACL function
	 */
	public SHACLSPARQLARQFunction(SHSPARQLFunction shaclFunction) {
		
		super(shaclFunction);
		
		try {
			queryString = shaclFunction.getSPARQL();
			arqQuery = ARQFactory.get().createQuery(SPARQLSubstitutions.withPrefixes(queryString, shaclFunction));
		}
		catch(Exception ex) {
			throw new IllegalArgumentException("Function " + shaclFunction.getURI() + " does not define a valid body", ex);
		}
		if(!arqQuery.isAskType() && !arqQuery.isSelectType()) {
            throw new ExprEvalException("Body must be ASK or SELECT query");
		}

		addParameters(shaclFunction);
	}

	
	@Override
    public org.apache.jena.sparql.function.Function create(String uri) {
		return this;
	}
	
	
	private QueryExecution createQueryExecution(Dataset dataset, Model defaultModel, QuerySolution bindings) {
	    if(dataset == null) {
            return ARQFactory.get().createQueryExecution(arqQuery, defaultModel, bindings);
	    }
	    else {
	    	Dataset newDataset = new DatasetWithDifferentDefaultModel(defaultModel, dataset);
	    	return ARQFactory.get().createQueryExecution(arqQuery, newDataset, bindings);
	    }
	}
	
	
	@Override
    public NodeValue executeBody(Dataset dataset, Model defaultModel, QuerySolution bindings) {
	    try( QueryExecution qexec = createQueryExecution(dataset, defaultModel, bindings) ) {
	        if(arqQuery.isAskType()) {
	            boolean result = qexec.execAsk();
	            return NodeValue.makeBoolean(result);
	        }
	        else {
	            ResultSet rs = qexec.execSelect();
	            if(rs.hasNext()) {
	                QuerySolution s = rs.nextSolution();
	                List<String> resultVars = rs.getResultVars();
	                String varName = resultVars.get(0);
	                RDFNode resultNode = s.get(varName);
	                if(resultNode != null) {
	                    return NodeValue.makeNode(resultNode.asNode());
	                }
	            }
	            throw new ExprEvalException("Empty result set for SHACL function");
	        }
	    }
	}
	

	/**
	 * Gets the Jena Query object for execution.
	 * @return the Jena Query
	 */
	public org.apache.jena.query.Query getBodyQuery() {
		return arqQuery;
	}


	@Override
	protected String getQueryString() {
		return queryString;
	}
}
