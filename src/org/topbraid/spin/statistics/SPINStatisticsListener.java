package org.topbraid.spin.statistics;


/**
 * An interface for objects interested in updates to the SPINStatisticsManager.
 * This can be used to track the execution of SPIN with real-time updates.
 * 
 * @author Holger Knublauch
 */
public interface SPINStatisticsListener {

	/**
	 * Called whenever the statistics have been updated.
	 */
	void statisticsUpdated();
}
