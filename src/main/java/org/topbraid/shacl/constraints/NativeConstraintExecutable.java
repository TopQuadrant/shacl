package org.topbraid.shacl.constraints;

import java.util.LinkedList;
import java.util.List;

import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.model.SHACLShape;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A ConstrainExecutable representing a directly executable constraint,
 * e.g. backed by a sh:sparql query.
 * 
 * @author Holger Knublauch
 */
public class NativeConstraintExecutable extends ConstraintExecutable {
	
	private Resource resource;
	
	
	public NativeConstraintExecutable(Resource resource) {
		this.resource = resource;
	}
	
	
	public List<Literal> getMessages() {
		return JenaUtil.getLiteralProperties(resource, SH.message);
	}
	
	
	public Resource getPredicate() {
		return JenaUtil.getResourceProperty(resource, SH.predicate);
	}
	
	
	public Resource getResource() {
		return resource;
	}


	@Override
	public List<SHACLShape> getScopeShapes() {
		List<SHACLShape> results = new LinkedList<SHACLShape>();
		for(Resource scope : JenaUtil.getResourceProperties(resource, SH.scopeShape)) {
			results.add(SHACLFactory.asShape(scope));
		}
		return results;
	}
	
	
	public Resource getSeverity() {
		Resource result = JenaUtil.getResourceProperty(resource, SH.severity);
		return result == null ? SH.Error : result;
	}


	public String toString() {
		return "NativeConstraintExecutable " + resource;
	}
}
