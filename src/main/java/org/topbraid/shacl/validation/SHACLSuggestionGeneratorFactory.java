package org.topbraid.shacl.validation;

import org.apache.jena.rdf.model.Model;

/**
 * A singleton object that can create SHACLSuggestionGenerators.
 * 
 * @author Holger Knublauch
 */
public class SHACLSuggestionGeneratorFactory {
	
	private static SHACLSuggestionGeneratorFactory singleton = new SHACLSuggestionGeneratorFactory();
	
	public static SHACLSuggestionGeneratorFactory get() {
		return singleton;
	}
	
	public static void set(SHACLSuggestionGeneratorFactory value) {
		singleton = value;
	}
	
	
	/**
	 * Default implementation returns nothing - no implementation is provided as part of the
	 * open source package.
	 * @param dataModel  the data graph to operate on
	 * @param shapesModel  the shapes graph to operate on
	 * @return a {@link SHACLSuggestionGenerator} or null
	 */
	public SHACLSuggestionGenerator createSuggestionGenerator(Model dataModel, Model shapesModel) {
		return null;
	}
}
