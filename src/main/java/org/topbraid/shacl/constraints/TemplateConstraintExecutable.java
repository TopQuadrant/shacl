package org.topbraid.shacl.constraints;

import java.util.LinkedList;
import java.util.List;

import org.topbraid.shacl.model.SHACLArgument;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.model.SHACLShape;
import org.topbraid.shacl.model.SHACLTemplate;
import org.topbraid.shacl.model.SHACLTemplateConstraint;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;

import org.apache.jena.rdf.model.Resource;

/**
 * A ConstraintExecutable backed by a template call.
 * 
 * @author Holger Knublauch
 */
public class TemplateConstraintExecutable extends ConstraintExecutable {
	
	private SHACLTemplateConstraint constraint;
	
	private SHACLTemplate template;
	
	
	public TemplateConstraintExecutable(SHACLTemplateConstraint constraint, SHACLTemplate template) {
		super(template);
		this.constraint = constraint;
		this.template = template;
	}


	public List<SHACLShape> getFilterShapes() {
		List<SHACLShape> results = new LinkedList<SHACLShape>();
		for(Resource scope : JenaUtil.getResourceProperties(constraint, SH.filterShape)) {
			results.add(SHACLFactory.asShape(scope));
		}
		return results;
	}
	
	
	@Override
	public Resource getSeverity() {
		Resource override = JenaUtil.getResourceProperty(constraint, SH.severity);
		if(override != null) {
			return override;
		}
		else {
			return super.getSeverity();
		}
	}


	public SHACLTemplateConstraint getTemplateCall() {
		return constraint;
	}
	
	
	public Resource getValidationFunction() {
		if(JenaUtil.hasIndirectType(template, SH.NodeConstraintTemplate) ||
				JenaUtil.hasIndirectType(template, SH.PropertyValueConstraintTemplate) ||
				JenaUtil.hasIndirectType(template, SH.InversePropertyValueConstraintTemplate)) {
			return template.getPropertyResourceValue(SH.validationFunction);
		}
		else {
			return null;
		}
	}
	
	
	/**
	 * Checks if all non-optional arguments are present.
	 * Also returns false if the template is abstract and has at least one argument but no arguments are provided.
	 * @return true  if complete
	 */
	public boolean isComplete() {
		boolean hasDirectArgumentValue = false;
		for(SHACLArgument arg : template.getArguments()) {
			boolean hasValue = constraint.hasProperty(arg.getPredicate()) || arg.getDefaultValue() != null;
			if(!arg.isOptional() && !hasValue) {
				if(!(arg.hasProperty(SH.optionalWhenInherited, JenaDatatypes.TRUE) && arg.isOptionalAtTemplate(template))) {
					return false;
				}
			}
			if(hasValue && template.hasProperty(SH.argument, arg)) {
				hasDirectArgumentValue = true;
			}
		}
		return !template.isAbstract() || !template.hasProperty(SH.argument) || hasDirectArgumentValue;
	}


	public String toString() {
		return SPINLabels.get().getLabel(template);
	}
}
