package org.topbraid.shacl.engine;

import org.apache.jena.rdf.model.Model;

/**
 * The singleton that should be used to construct ShapesGraph instances.
 * Can be overloaded to make customizations.
 * 
 * @author Holger Knublauch
 */
public class ShapesGraphFactory {

	private static ShapesGraphFactory singleton = new ShapesGraphFactory();
	
	public static ShapesGraphFactory get() {
		return singleton;
	}
	
	public static void set(ShapesGraphFactory value) {
		ShapesGraphFactory.singleton = value;
	}
	
	
	public ShapesGraph createShapesGraph(Model shapesModel) {
		return new ShapesGraph(shapesModel);
	}
}
