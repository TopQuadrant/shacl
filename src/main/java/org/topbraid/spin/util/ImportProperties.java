package org.topbraid.spin.util;

import java.util.Arrays;
import java.util.List;

import org.topbraid.shacl.vocabulary.SH;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * A singleton controlling which properties shall be used to expand imports.
 * This includes owl:imports.
 * 
 * @author Holger Knublauch
 */
public class ImportProperties {

	private static ImportProperties singleton = new ImportProperties();
	
	public static ImportProperties get() {
		return singleton;
	}
	
	public static void set(ImportProperties value) {
		singleton = value;
	}
	
	
	private List<Property> results = Arrays.asList(new Property[] {
		OWL.imports,
		SH.shapesGraph
	});
	
	
	public List<Property> getImportProperties() {
		return results;
	}
}
