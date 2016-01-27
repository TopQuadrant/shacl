package org.topbraid.shacl.constraints;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

/**
 * A singleton that determines which constraints should be excluded if
 * the ModelConstraintChecker is running on filtered mode.
 * 
 * @author Holger Knublauch
 */
public class ModelClassesFilter {
	
	private static ModelClassesFilter singleton = new ModelClassesFilter();
	
	public static ModelClassesFilter get() {
		return singleton;
	}
	
	public static void set(ModelClassesFilter value) {
		singleton = value;
	}
	
	
	private static Set<Resource> SYSTEM_CLASSES = new HashSet<Resource>();
	
	static {
		SYSTEM_CLASSES.addAll(Arrays.asList(new Resource[] {
			OWL.Ontology,
			RDFS.Class,
			RDFS.Resource,
			SH.AbstractResult,
			SH.Constraint,
			SH.ResultAnnotation,
			SH.Shape
		}));
	}

	
	public boolean accept(Resource cls) {
		
		if(SYSTEM_CLASSES.contains(cls)) {
			return false;
		}
		
		if(JenaUtil.hasIndirectType(cls, SH.Macro)) {
			return false;
		}
		
		if(RDFS.class.equals(cls) || JenaUtil.hasSuperClass(cls, RDFS.Class.inModel(cls.getModel()))) {
			return false;
		}
		
		return true;
	}
}
