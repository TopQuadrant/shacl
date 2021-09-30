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

import java.net.URI;
import java.util.Collections;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.jenax.functions.AbstractFunction3;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.engine.ShapesGraphFactory;
import org.topbraid.shacl.util.FailureLog;
import org.topbraid.shacl.util.RecursionGuard;
import org.topbraid.shacl.validation.DefaultShapesGraphProvider;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.validation.ValidationEngineConfiguration;
import org.topbraid.shacl.validation.ValidationEngineFactory;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

/**
 * The implementation of the tosh:hasShape function.
 * 
 * @author Holger Knublauch
 */
public class HasShapeFunction extends AbstractFunction3 {
	
	private static ThreadLocal<Boolean> recursionIsErrorFlag = new ThreadLocal<Boolean>();
	
	private static ThreadLocal<Model> resultsModelTL = new ThreadLocal<>();
	
	private static ThreadLocal<ShapesGraph> shapesGraph = new ThreadLocal<>();
	
	private static ThreadLocal<URI> shapesGraphURI = new ThreadLocal<URI>();
	
	public static Model getResultsModel() {
		return resultsModelTL.get();
	}
	
	public static ShapesGraph getShapesGraph() {
		return shapesGraph.get();
	}
	
	public static URI getShapesGraphURI() {
		return shapesGraphURI.get();
	}
	
	public static void setResultsModel(Model value) {
		resultsModelTL.set(value);
	}
	
	public static void setShapesGraph(ShapesGraph value, URI uri) {
		if(value == null && uri != null) {
			Model shapesModel = ARQFactory.getNamedModel(uri.toString());
			value = new ShapesGraph(shapesModel);
		}
		shapesGraph.set(value);
		shapesGraphURI.set(uri);
	}

	
	@Override
	protected NodeValue exec(Node focusNode, Node shapeNode, Node recursionIsError, FunctionEnv env) {
		return exec(focusNode, shapeNode, recursionIsError, env.getActiveGraph(), DatasetFactory.wrap(env.getDataset()));
	}
	
	
	public static NodeValue exec(Node focusNode, Node shapeNode, Node recursionIsError, Graph activeGraph, Dataset dataset) {

		Boolean oldFlag = recursionIsErrorFlag.get();
		if(JenaDatatypes.TRUE.asNode().equals(recursionIsError)) {
			recursionIsErrorFlag.set(true);
		}
		try {
			if(RecursionGuard.start(focusNode, shapeNode)) {
				RecursionGuard.end(focusNode, shapeNode);
				if(JenaDatatypes.TRUE.asNode().equals(recursionIsError) || (oldFlag != null && oldFlag)) {
					String message = "Unsupported recursion";
					Model resultsModel = resultsModelTL.get();
					Resource failure = resultsModel.createResource(DASH.FailureResult);
					failure.addProperty(SH.resultMessage, message);
					failure.addProperty(SH.focusNode, resultsModel.asRDFNode(focusNode));
					failure.addProperty(SH.sourceShape, resultsModel.asRDFNode(shapeNode));
					FailureLog.get().logFailure(message);
					throw new ExprEvalException("Unsupported recursion");
				}
				else {
					return NodeValue.TRUE;
				}
			}
			else {
				
				try {
					Model model = ModelFactory.createModelForGraph(activeGraph);
					RDFNode resource = model.asRDFNode(focusNode);
					Resource shape = (Resource) dataset.getDefaultModel().asRDFNode(shapeNode);
					Model results = doRun(resource, shape, dataset);
					if(resultsModelTL.get() != null) {
						resultsModelTL.get().add(results);
					}
					if(results.contains(null, RDF.type, DASH.FailureResult)) {
						throw new ExprEvalException("Propagating failure from nested shapes");
					}

					if(ValidationEngine.getCurrent() != null && ValidationEngine.getCurrent().getConfiguration().getReportDetails()) {
						boolean result = true;
						for(Resource r : results.listSubjectsWithProperty(RDF.type, SH.ValidationResult).toList()) {
							if(!results.contains(null, SH.detail, r)) {
								result = false;
								break;
							}
						}
						return NodeValue.makeBoolean(result);
					}
					else {
						boolean result = !results.contains(null, RDF.type, SH.ValidationResult);
						return NodeValue.makeBoolean(result);
					}
				}
				finally {
					RecursionGuard.end(focusNode, shapeNode);
				}
			}
		}
		finally {
			recursionIsErrorFlag.set(oldFlag);
		}
	}


	private static Model doRun(RDFNode focusNode, Resource shape, Dataset dataset) {
		URI sgURI = shapesGraphURI.get();
		ShapesGraph sg = shapesGraph.get();
		if(sgURI == null) {
			sgURI = DefaultShapesGraphProvider.get().getDefaultShapesGraphURI(dataset);
			Model shapesModel = dataset.getNamedModel(sgURI.toString());
			sg = ShapesGraphFactory.get().createShapesGraph(shapesModel);
		}
		else if(sg == null) {
			Model shapesModel = dataset.getNamedModel(sgURI.toString());
			sg = ShapesGraphFactory.get().createShapesGraph(shapesModel);
			shapesGraph.set(sg);
		}
		Model reportModel = JenaUtil.createMemoryModel();
		Resource report = reportModel.createResource(SH.ValidationReport); // This avoids the expensive setNsPrefixes call in ValidationEngine constructor
		ValidationEngine engine = ValidationEngineFactory.get().create(dataset, sgURI, sg, report);
		if(ValidationEngine.getCurrent() != null) {
			ValidationEngineConfiguration cloned = ValidationEngine.getCurrent().getConfiguration().clone();
			cloned.setValidationErrorBatch(-1);
			engine.setConfiguration(cloned);
		}
		engine.validateNodesAgainstShape(Collections.singletonList(focusNode), shape.asNode());
		return reportModel;
	}
}
