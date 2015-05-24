package org.topbraid.shacl.constraints;

import java.util.LinkedList;
import java.util.List;

import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.model.SHACLShape;
import org.topbraid.shacl.vocabulary.SHACL;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

public class NativeConstraintExecutable extends ConstraintExecutable {
	
	private Resource resource;
	
	
	public NativeConstraintExecutable(Resource resource) {
		this.resource = resource;
	}
	
	
	public List<Literal> getMessages() {
		return JenaUtil.getLiteralProperties(resource, SHACL.message);
	}
	
	
	public Resource getPredicate() {
		return JenaUtil.getResourceProperty(resource, SHACL.predicate);
	}
	
	
	public Resource getResource() {
		return resource;
	}


	@Override
	public List<SHACLShape> getScopes() {
		List<SHACLShape> results = new LinkedList<SHACLShape>();
		for(Resource scope : JenaUtil.getResourceProperties(resource, SHACL.scopeShape)) {
			results.add(SHACLFactory.asShape(scope));
		}
		return results;
	}
	
	
	/**
	 * Gets the severity, if specified (e.g. sh:Warning).
	 * @return the severity class, never null
	 */
	public Resource getSeverity() {
		Resource result = JenaUtil.getResourceProperty(resource, SHACL.severity);
		return result == null ? SHACL.Error : result;
	}


	public String toString() {
		return "NativeConstraintExecutable " + resource;
	}
}
