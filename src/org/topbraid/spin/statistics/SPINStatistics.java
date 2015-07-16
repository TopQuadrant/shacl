/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.statistics;

import com.hp.hpl.jena.graph.Node;


/**
 * A wrapper to record the execution time of a given Query
 * for statistical purposes.
 * 
 * @author Holger Knublauch
 */
public class SPINStatistics {
	
	private Node context;

	private long duration;
	
	private String label;
	
	private String queryText;
	
	private long startTime;
	
	
	/**
	 * Creates a new SPINStatistics object.
	 * @param label  the label of the action that has been measured
	 * @param queryText  the text of the query that was executed
	 * @param duration  the total duration in ms
	 * @param startTime  the start time of execution (for ordering)
	 * @param context  the Node that for example was holding the spin:rule
	 */
	public SPINStatistics(String label, String queryText, long duration, long startTime, Node context) {
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
