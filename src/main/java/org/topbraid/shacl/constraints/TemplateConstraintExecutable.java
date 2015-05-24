package org.topbraid.shacl.constraints;

import java.util.LinkedList;
import java.util.List;

import org.topbraid.shacl.model.SHACLArgument;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.model.SHACLShape;
import org.topbraid.shacl.model.SHACLTemplate;
import org.topbraid.shacl.model.SHACLTemplateConstraint;
import org.topbraid.shacl.vocabulary.SHACL;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

public class TemplateConstraintExecutable extends ConstraintExecutable {
	
	private SHACLTemplateConstraint constraint;
	
	private SHACLTemplate template;
	
	
	public TemplateConstraintExecutable(SHACLTemplateConstraint constraint, SHACLTemplate template) {
		this.constraint = constraint;
		this.template = template;
	}
	
	
	public SHACLTemplateConstraint getConstraint() {
		return constraint;
	}
	
	
	public List<Literal> getMessages() {
		return JenaUtil.getLiteralProperties(template, SHACL.message);
	}
	
	
	public Resource getPredicate() {
		return JenaUtil.getResourceProperty(template, SHACL.predicate);
	}


	public List<SHACLShape> getScopes() {
		List<SHACLShape> results = new LinkedList<SHACLShape>();
		for(Resource scope : JenaUtil.getResourceProperties(constraint, SHACL.scopeShape)) {
			results.add(SHACLFactory.asShape(scope));
		}
		return results;
	}

	
	/**
	 * Gets the severity level, if specified (e.g. sh:Warning).
	 * @return the severity class, never null
	 */
	public Resource getSeverity() {
		Resource severity = JenaUtil.getResourceProperty(template, SHACL.severity);
		return severity == null ? SHACL.Error : severity;
	}
	
	
	public SHACLTemplate getTemplate() {
		return template;
	}
	
	
	/**
	 * Checks if all non-optional argument are present.
	 * @return true  if complete
	 */
	public boolean isComplete() {
		for(SHACLArgument arg : template.getArguments(false)) {
			if(!arg.isOptional() && !constraint.hasProperty(arg.getPredicate()) && arg.getDefaultValue() == null) {
				if(!(arg.hasProperty(SHACL.optionalWhenInherited, JenaDatatypes.TRUE) && arg.isOptionalAtTemplate(template))) {
					return false;
				}
			}
		}
		return true;
	}


	public String toString() {
		return "TemplateConstraintExecutable of type " + template.getURI();
	}
}
