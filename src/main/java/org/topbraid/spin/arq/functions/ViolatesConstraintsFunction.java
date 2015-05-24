package org.topbraid.spin.arq.functions;

import java.util.LinkedList;
import java.util.List;

import org.topbraid.spin.arq.AbstractFunction2;
import org.topbraid.spin.constraints.ConstraintViolation;
import org.topbraid.spin.constraints.SPINConstraints;
import org.topbraid.spin.model.QueryOrTemplateCall;
import org.topbraid.spin.progress.NullProgressMonitor;
import org.topbraid.spin.progress.ProgressMonitor;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.Function;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.function.FunctionFactory;

public class ViolatesConstraintsFunction extends AbstractFunction2  implements FunctionFactory {
	
	@Override
	public Function create(String uri) {
		return this;
	}

	
	@Override
	protected NodeValue exec(Node instanceNode, Node classNode, FunctionEnv env) {
		
		Model model = ModelFactory.createModelForGraph(env.getActiveGraph());
		
		// Collect all constraints defined at the class and its superclasses
		Resource cls = (Resource)model.asRDFNode(classNode);
		
		List<QueryOrTemplateCall> qots = ConstructViolationsPFunction.getConstraints(cls);

		Resource instance = (Resource) model.asRDFNode(instanceNode);
		ProgressMonitor monitor = new NullProgressMonitor();
		List<ConstraintViolation> results = new LinkedList<ConstraintViolation>();
		for(QueryOrTemplateCall qot : qots) {
			if(qot.getTemplateCall() != null) {
				SPINConstraints.addTemplateCallResults(results, qot, instance, false, monitor);
			}
			else if(qot.getQuery() != null) {
				SPINConstraints.addQueryResults(results, qot, instance, false, null, monitor);
			}
			if(!results.isEmpty()) {
				return NodeValue.makeBoolean(true);
			}
		}

		return NodeValue.makeBoolean(false);
	}
}
