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

import org.apache.jena.graph.Node;


/**
 * A wrapper to record the execution time of some processing step for statistical purposes.
 * Typical use cases include SPARQL queries or expressions.
 * 
 * @author Holger Knublauch
 */
public class ExecStatistics {
	
	private Node context;

	private long duration;
	
	private String label;
	
	private String queryText;
	
	private long startTime;
	
	
	/**
	 * Creates a new ExecStatistics object.
	 * @param label  the label of the action that has been measured
	 * @param queryText  the text of the query that was executed
	 * @param duration  the total duration in ms
	 * @param startTime  the start time of execution (for ordering)
	 * @param context  the Node that for example was holding the spin:rule
	 */
	public ExecStatistics(String label, String queryText, long duration, long startTime, Node context) {
		this.context = context;
		this.duration = duration;
		this.label = label;
		this.queryText = queryText;
		this.startTime = startTime;
	}
	
	
	public Node getContext() {
		return context;
	}
	
	
	public long getDuration() {
		return duration;
	}
	
	
	public String getLabel() {
		return label;
	}
	
	
	public String getQueryText() {
		return queryText;
	}
	
	
	public long getStartTime() {
		return startTime;
	}
}
