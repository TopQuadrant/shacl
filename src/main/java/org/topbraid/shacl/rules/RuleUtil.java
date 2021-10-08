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
package org.topbraid.shacl.rules;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.jenax.progress.ProgressMonitor;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.arq.SHACLFunctions;
import org.topbraid.shacl.engine.SHACLScriptEngineManager;
import org.topbraid.shacl.engine.Shape;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.engine.ShapesGraphFactory;
import org.topbraid.shacl.util.SHACLSystemModel;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.shacl.vocabulary.TOSH;

/**
 * Convenience methods to execute SHACL rules.
 * 
 * @author Holger Knublauch
 */
public class RuleUtil {

	/**
	 * Executes all rules from a given shapes Model on a given data Model.
	 * If the shapesModel does not include the system graph triples then these will be added.
	 * If inferencesModel is not null then it must be part of the dataModel (e.g. a sub-graph)
	 * of a Jena MultiUnion object.
	 * Otherwise, the function will create a new inferences Model which is merged with the
	 * dataModel for the duration of the execution.
	 * @param dataModel  the data Model
	 * @param shapesModel  the shapes Model
	 * @param inferencesModel  the Model for the inferred triples or null
	 * @param monitor  an optional progress monitor
	 * @return the Model of inferred triples (i.e. inferencesModel if not null, or a new Model)
	 */
	public static Model executeRules(Model dataModel, Model shapesModel, Model inferencesModel, ProgressMonitor monitor) {
		return executeRulesHelper(dataModel, null, shapesModel, inferencesModel, monitor);
	}


	/**
	 * Executes all rules from a given shapes Model on a given focus node (in its data Model).
	 * This only executes the rules from the shapes that have the focus node in their target.
	 * If the shapesModel does not include the system graph triples then these will be added.
	 * If inferencesModel is not null then it must be part of the dataModel (e.g. a sub-graph)
	 * of a Jena MultiUnion object.
	 * Otherwise, the function will create a new inferences Model which is merged with the
	 * dataModel for the duration of the execution.
	 * @param focusNode  the focus node in the data Model
	 * @param shapesModel  the shapes Model
	 * @param inferencesModel  the Model for the inferred triples or null
	 * @param monitor  an optional progress monitor
	 * @return the Model of inferred triples (i.e. inferencesModel if not null, or a new Model)
	 */	
	public static Model executeRules(RDFNode focusNode, Model shapesModel, Model inferencesModel, ProgressMonitor monitor) {
		return executeRulesHelper(focusNode.getModel(), focusNode, shapesModel, inferencesModel, monitor);
	}
	
	
	private static Model executeRulesHelper(Model dataModel, RDFNode focusNode, Model shapesModel, Model inferencesModel, ProgressMonitor monitor) {

		// Ensure that the SHACL, DASH and TOSH graphs are present in the shapes Model
		if(!shapesModel.contains(TOSH.hasShape, RDF.type, (RDFNode)null)) { // Heuristic
			Model unionModel = SHACLSystemModel.getSHACLModel();
			MultiUnion unionGraph = new MultiUnion(new Graph[] {
				unionModel.getGraph(),
				shapesModel.getGraph()
			});
			unionGraph.setBaseGraph(shapesModel.getGraph());
			shapesModel = ModelFactory.createModelForGraph(unionGraph);
		}

		// Make sure all sh:Functions are registered
		SHACLFunctions.registerFunctions(shapesModel);
		
		if(inferencesModel == null) {
			inferencesModel = JenaUtil.createDefaultModel();
			inferencesModel.setNsPrefixes(dataModel);
			inferencesModel.withDefaultMappings(shapesModel);
			MultiUnion unionGraph = new MultiUnion(new Graph[] {
				dataModel.getGraph(),
				inferencesModel.getGraph()
			});
			dataModel = ModelFactory.createModelForGraph(unionGraph);
		}
		
		// Create Dataset that contains both the data model and the shapes model
		// (here, using a temporary URI for the shapes graph)
		URI shapesGraphURI = SHACLUtil.createRandomShapesGraphURI();
		Dataset dataset = ARQFactory.get().getDataset(dataModel);
		dataset.addNamedModel(shapesGraphURI.toString(), shapesModel);

		ShapesGraph shapesGraph = ShapesGraphFactory.get().createShapesGraph(shapesModel);
		RuleEngine engine = new RuleEngine(dataset, shapesGraphURI, shapesGraph, inferencesModel);
		engine.setProgressMonitor(monitor);
		
		boolean nested = SHACLScriptEngineManager.get().begin();
		try {
			engine.applyEntailments();
			if(focusNode == null) {
				engine.setExcludeNeverMaterialize(true);
				engine.executeAll();
			}
			else {
				List<Shape> shapes = getShapesWithTargetNode(focusNode, shapesGraph);
				engine.executeShapes(shapes, focusNode);
			}
		}
		catch(InterruptedException ex) {
			return null;
		}
		finally {
			SHACLScriptEngineManager.get().end(nested);
		}
		return inferencesModel;
	}


	public static List<Shape> getShapesWithTargetNode(RDFNode focusNode, ShapesGraph shapesGraph) {
		// TODO: Not a particularly smart algorithm - walks all shapes that have rules
		List<Shape> shapes = new ArrayList<>();
		for(Shape shape : shapesGraph.getRootShapes()) {
			if(shape.getShapeResource().hasProperty(SH.rule) && shape.getShapeResource().hasTargetNode(focusNode)) {
				shapes.add(shape);
			}
		}
		return shapes;
	}
}
