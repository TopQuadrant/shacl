package org.topbraid.shacl.validation;

import java.util.Collections;

import org.topbraid.jenax.statistics.ExecStatistics;
import org.topbraid.jenax.statistics.ExecStatisticsManager;
import org.topbraid.shacl.engine.Constraint;

public abstract class AbstractNativeConstraintExecutor implements ConstraintExecutor {

	protected void addStatistics(Constraint constraint, long startTime) {
		if(ExecStatisticsManager.get().isRecording()) {
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			ExecStatistics stats = new ExecStatistics(constraint.getComponent().getLocalName() + " (Native constraint executor)", null, duration, startTime, constraint.getComponent().asNode());
			ExecStatisticsManager.get().add(Collections.singletonList(stats));
		}
	}
}
