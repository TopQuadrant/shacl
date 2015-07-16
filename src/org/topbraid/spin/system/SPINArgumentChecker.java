package org.topbraid.spin.system;

import java.util.LinkedList;
import java.util.List;

import org.topbraid.spin.model.Argument;
import org.topbraid.spin.model.Module;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * A singleton that is used by SPINARQFunction to check whether all supplied arguments
 * match the definition of the declared spl:Arguments.
 * 
 * By default the singleton is null (indicating a no-op), but implementors can install a
 * subclass of this to report warnings, throw exceptions or whatever they like.
 * 
 * Note that activating this will have a severe performance impact.
 * 
 * @author Holger Knublauch
 */
public abstract class SPINArgumentChecker {

	private static SPINArgumentChecker singleton;
	
	public static SPINArgumentChecker get() {
		return singleton;
	}
	
	public static void set(SPINArgumentChecker value) {
		singleton = value;
	}
	

	public void check(Module module, QuerySolutionMap bindings) {
		List<String> errors = new LinkedList<String>();
		for(Argument arg : module.getArguments(false)) {
			String varName = arg.getVarName();
			RDFNode value = bindings.get(varName);
			if(!arg.isOptional() && value == null) {
				errors.add("Missing required argument " + varName);
			}
			else if(value != null) {
				Resource valueType = arg.getValueType();
				if(valueType != null) {
					if(value.isResource()) {
						if(!RDFS.Resource.equals(valueType) && !JenaUtil.hasIndirectType((Resource)value, valueType.inModel(value.getModel()))) {
							StringBuffer sb = new StringBuffer("Resource ");
							sb.append(SPINLabels.get().getLabel((Resource)value));
							sb.append(" for argument ");
							sb.append(varName);
							sb.append(" must have type ");
							sb.append(SPINLabels.get().getLabel(valueType));
							errors.add(sb.toString());
						}
					}
					else if(!RDFS.Literal.equals(valueType)) {
						String datatypeURI = value.asLiteral().getDatatypeURI();
						if(datatypeURI == null) {
							datatypeURI = XSD.xstring.getURI();
						}
						if(!valueType.getURI().equals(datatypeURI)) {
							StringBuffer sb = new StringBuffer("Literal ");
							sb.append(value.asLiteral().getLexicalForm());
							sb.append(" for argument ");
							sb.append(varName);
							sb.append(" must have datatype ");
							sb.append(SPINLabels.get().getLabel(valueType));
							errors.add(sb.toString());
						}
					}
				}
			}
		}
		if(!errors.isEmpty()) {
			handleErrors(module, bindings, errors);
		}
	}
	
	
	protected abstract void handleErrors(Module module, QuerySolutionMap bindings, List<String> errors);
}
