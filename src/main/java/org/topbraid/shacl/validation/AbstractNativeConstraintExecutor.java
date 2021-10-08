package org.topbraid.shacl.validation;

import java.net.URI;
import java.util.Collections;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.topbraid.jenax.statistics.ExecStatistics;
import org.topbraid.jenax.statistics.ExecStatisticsManager;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.arq.functions.HasShapeFunction;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.util.FailureLog;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

public abstract class AbstractNativeConstraintExecutor implements ConstraintExecutor {

	protected void addStatistics(ValidationEngine engine, Constraint constraint, long startTime, int focusNodeCount, long valueNodeCount) {
		if(ExecStatisticsManager.get().isRecording()) {
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			ExecStatistics stats = new ExecStatistics(constraint.getComponent().getLocalName() + " (Native constraint executor)", null, duration, startTime, constraint.getComponent().asNode());
			ExecStatisticsManager.get().add(Collections.singletonList(stats));
		}
		if(engine.getProfile() != null) {
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			engine.getProfile().record(duration, focusNodeCount, valueNodeCount, constraint);
		}
	}
	
	
	protected Model hasShape(ValidationEngine engine, Constraint constraint, RDFNode focusNode, RDFNode valueNode, RDFNode shape, boolean recursionIsError) {
		URI oldShapesGraphURI = HasShapeFunction.getShapesGraphURI();
		ShapesGraph oldShapesGraph = HasShapeFunction.getShapesGraph();		
		Model oldNestedResults = HasShapeFunction.getResultsModel();
		try {
			if(!engine.getShapesGraphURI().equals(oldShapesGraphURI)) {
				HasShapeFunction.setShapesGraph(engine.getShapesGraph(), engine.getShapesGraphURI());
			}
			Model nestedResults = JenaUtil.createMemoryModel();
			HasShapeFunction.setResultsModel(nestedResults);				
			try {
				NodeValue result = HasShapeFunction.exec(valueNode.asNode(), shape.asNode(), recursionIsError ? JenaDatatypes.TRUE.asNode() : null, engine.getDataset().getDefaultModel().getGraph(), engine.getDataset());
				if(NodeValue.TRUE.equals(result)) {
					return null;
				}
				else {
					return nestedResults;
				}
			}
			catch(ExprEvalException ex) {
				String message = constraint + " has produced a failure for focus node " + engine.getLabelFunction().apply(focusNode);
				FailureLog.get().logFailure(message);
				Resource result = nestedResults.createResource(DASH.FailureResult);
				result.addProperty(SH.resultSeverity, constraint.getSeverity());
				result.addProperty(SH.sourceConstraintComponent, constraint.getComponent());
				result.addProperty(SH.sourceShape, constraint.getShapeResource());
				result.addProperty(SH.focusNode, focusNode);
				result.addProperty(SH.value, valueNode);
				result.addProperty(SH.resultMessage, message);
				return nestedResults;
			}
		}
		finally {
			HasShapeFunction.setShapesGraph(oldShapesGraph, oldShapesGraphURI);
			HasShapeFunction.setResultsModel(oldNestedResults);
		}
	}
}
