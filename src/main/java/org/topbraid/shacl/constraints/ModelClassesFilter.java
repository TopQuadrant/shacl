package org.topbraid.shacl.constraints;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

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
			RDFS.Class,
			RDFS.Resource,
			SH.ConstraintViolation,
			SH.Graph,
			SH.NativeConstraint
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
