package org.topbraid.shacl.validation;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.jenax.util.JenaUtil;

/**
 * An object that is used to cache subclasses of all classes mentioned in sh:class statements,
 * for faster execution of sh:class checking, avoiding repeated traversal of the subClassOf hierarchy.
 * 
 * @author Holger Knublauch
 */
public class ClassesCache {
	
	private Map<Resource,Predicate<Resource>> predicates = new ConcurrentHashMap<>();
	
	
	public Predicate<Resource> getPredicate(Resource cls) {
		return predicates.computeIfAbsent(cls, c -> {
			Set<Resource> classes = JenaUtil.getAllSubClassesStar(c);
			if(classes.size() == 1) {
				return new ClassPredicate(c);
			}
			else {
				return new SubClassesPredicate(classes);
			}
		});
	}
	
	
	private static class ClassPredicate implements Predicate<Resource> {
		
		private Resource cls;
		
		ClassPredicate(Resource cls) {
			this.cls = cls;
		}
		
		@Override
		public boolean test(Resource instance) {
			return instance.hasProperty(RDF.type, cls);
		}
	}
	
	
	private static class SubClassesPredicate implements Predicate<Resource> {
		
		private Set<Resource> classes;
		
		SubClassesPredicate(Set<Resource> classes) {
			this.classes = classes;
		}

		@Override
		public boolean test(Resource instance) {
			StmtIterator it = instance.listProperties(RDF.type);
			while(it.hasNext()) {
				if(classes.contains(it.next().getObject())) {
					it.close();
					return true;
				}
			}
			return false;
		}
	}
}
