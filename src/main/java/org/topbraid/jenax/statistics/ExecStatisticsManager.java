/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */
package org.topbraid.jenax.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * A singleton managing execution statistics.
 * In TopBraid, this singleton is used as a single entry point for various
 * statistics producing engines such as TopSPIN and the SHACL processors.
 * The results are displayed in the Statistics view of TBC.
 * 
 * The ExecStatisticsManager is off by default, and needs to be activated
 * with <code>setRecording(true);</code>.
 * 
 * @author Holger Knublauch
 */
public class ExecStatisticsManager {

	private static ExecStatisticsManager singleton = new ExecStatisticsManager();
	
	/**
	 * Gets the singleton instance of this class.
	 * @return the ExecStatisticsManager (never null)
	 */
	public static ExecStatisticsManager get() {
		return singleton;
	}
	
	
	private Set<ExecStatisticsListener> listeners = new HashSet<>();

	// Indicates whether recording is "on" in general
	private boolean recording;
	
	// Indicates whether calls to native SPARQL functions (implemented in Java) should be recorded
	private boolean recordingNativeFunctions;
	
	// Indicates whether calls to declarative SPARQL functions (e.g., via SHACL or SPIN) should be recorded
	private boolean recordingDeclarativeFunctions;
	
	private List<ExecStatistics> stats = Collections.synchronizedList(new LinkedList<>());
	
	
	public void addListener(ExecStatisticsListener listener) {
		listeners.add(listener);
	}
	

	/**
	 * Adds new statistics and notifies any registered listeners.
	 * This should only be called if <code>isRecording()</code> is true
	 * to prevent the unnecessary creation of SPINStatistics objects.
	 * @param values  the statistics to add
	 */
	public synchronized void add(Iterable<ExecStatistics> values) {
		addSilently(values);
		notifyListeners();
	}


	/**
	 * Adds new statistics without notifying listeners.
	 * This should only be called if <code>isRecording()</code> is true
	 * to prevent the unnecessary creation of SPINStatistics objects.
	 * @param values  the statistics to add
	 */
	public void addSilently(Iterable<ExecStatistics> values) {
		for(ExecStatistics s : values) {
			stats.add(s);
		}
	}
	
	
	/**
	 * Gets all previously added statistics.
	 * @return the statistics
	 */
	public synchronized List<ExecStatistics> getStatistics() {
		return stats;
	}
	
	
	public boolean isRecording() {
		return recording;
	}
	
	
	public boolean isRecordingDeclarativeFunctions() {
		return recordingDeclarativeFunctions;
	}
	
	
	public boolean isRecordingNativeFunctions() {
		return recordingNativeFunctions;
	}
	
	
	public void removeListener(ExecStatisticsListener listener) {
		listeners.remove(listener);
	}
	
	
	public synchronized void reset() {
		stats.clear();
		notifyListeners();
	}
	
	
	/**
	 * Notifies all registered listeners so that they can refresh themselves.
	 */
	public void notifyListeners() {
		for(ExecStatisticsListener listener : new ArrayList<>(listeners)) {
			listener.statisticsUpdated();
		}
	}
	
	
	public void setRecording(boolean value) {
		this.recording = value;
	}
	
	
	public void setRecordingDeclarativeFunctions(boolean value) {
		this.recordingDeclarativeFunctions = value;
	}
	
	
	public void setRecordingNativeFunctions(boolean value) {
		this.recordingNativeFunctions = value;
	}
}
