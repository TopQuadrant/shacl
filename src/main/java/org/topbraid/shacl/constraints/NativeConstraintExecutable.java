package org.topbraid.shacl.constraints;

import java.util.LinkedList;
import java.util.List;

import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.model.SHACLShape;
import org.topbraid.shacl.model.SHACLTemplateCall;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A ConstrainExecutable representing a directly executable constraint,
 * e.g. backed by a sh:sparql query.
 * 
 * @author Holger Knublauch
 */
public class NativeConstraintExecutable extends ConstraintExecutable {
	
	
	public NativeConstraintExecutable(Resource resource) {
		super(resource);
	}


	@Override
	public List<SHACLShape> getFilterShapes() {
		List<SHACLShape> results = new LinkedList<SHACLShape>();
		for(Resource scope : JenaUtil.getResourceProperties(getResource(), SH.filterShape)) {
			results.add(SHACLFactory.asShape(scope));
		}
		return results;
	}


	@Override
	public SHACLTemplateCall getTemplateCall() {
		return null;
	}


	public String toString() {
		return "Native SHACL Constraint";
	}
}
