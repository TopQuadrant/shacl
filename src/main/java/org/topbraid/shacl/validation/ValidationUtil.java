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
package org.topbraid.shacl.validation;

import java.net.URI;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.shacl.arq.SHACLFunctions;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.engine.ShapesGraphFactory;
import org.topbraid.shacl.util.SHACLSystemModel;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.TOSH;

/**
 * Convenience methods to perform SHACL validation.
 * 
 * These methods are provided for convenience of simple use cases only but are often not the most efficient way
 * of working with SHACL.  It is typically better to separate the creation of the ShapesGraph object from
 * the ValidationEngine because the ShapesGraph can be reused across multiple validations, and serves as a "pre-compiled"
 * data structure that is expensive to rebuild for each run.
 * 
 * Having separate calls also provides access to the other functions of the ValidationEngine object, such as
 * <code>validateNode</code> and <code>getValidationReport</code>.
 * 
 * @author Holger Knublauch
 */
public class ValidationUtil {


	public static ValidationEngine createValidationEngine(Model dataModel, Model shapesModel, boolean validateShapes) {
		return createValidationEngine(dataModel, shapesModel, new ValidationEngineConfiguration().setValidateShapes(validateShapes));
	}

	
	public static ValidationEngine createValidationEngine(Model dataModel, Model shapesModel, ValidationEngineConfiguration configuration) {

		shapesModel = ensureToshTriplesExist(shapesModel);

		// Make sure all sh:Functions are registered
		SHACLFunctions.registerFunctions(shapesModel);

		// Create Dataset that contains both the data model and the shapes model
		// (here, using a temporary URI for the shapes graph)
		URI shapesGraphURI = SHACLUtil.createRandomShapesGraphURI();
		Dataset dataset = ARQFactory.get().getDataset(dataModel);
		dataset.addNamedModel(shapesGraphURI.toString(), shapesModel);

		ShapesGraph shapesGraph = ShapesGraphFactory.get().createShapesGraph(shapesModel);

		ValidationEngine engine = ValidationEngineFactory.get().create(dataset, shapesGraphURI, shapesGraph, null);
		engine.setConfiguration(configuration);
		return engine;
	}


	public static Model ensureToshTriplesExist(Model shapesModel) {
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
		return shapesModel;
	}


	/**
	 * Validates a given data Model against all shapes from a given shapes Model.
	 * If the shapesModel does not include the system graph triples then these will be added.
	 * Entailment regimes are applied prior to validation.
	 * @param dataModel  the data Model
	 * @param shapesModel  the shapes Model
	 * @param validateShapes  true to also validate any shapes in the data Model (false is faster)
	 * @return an instance of sh:ValidationReport in a results Model
	 */
	public static Resource validateModel(Model dataModel, Model shapesModel, boolean validateShapes) {
		return validateModel(dataModel, shapesModel, new ValidationEngineConfiguration().setValidateShapes(validateShapes));
	}

	
	/**
	 * Validates a given data Model against all shapes from a given shapes Model.
	 * If the shapesModel does not include the system graph triples then these will be added.
	 * Entailment regimes are applied prior to validation.
	 * @param dataModel  the data Model
	 * @param shapesModel  the shapes Model
	 * @param configuration  configuration for the validation engine
	 * @return an instance of sh:ValidationReport in a results Model
	 */
	public static Resource validateModel(Model dataModel, Model shapesModel, ValidationEngineConfiguration configuration) {

		ValidationEngine engine = createValidationEngine(dataModel, shapesModel, configuration);
		engine.setConfiguration(configuration);
		try {
			engine.applyEntailments();
			return engine.validateAll();
		}
		catch(InterruptedException ex) {
			return null;
		}
	}
}
