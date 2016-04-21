/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.progress;


/**
 * A simple implementation of ProgressMonitor that prints messages
 * to System.out.
 *
 * @author Holger Knublauch
 */
public class SimpleProgressMonitor implements ProgressMonitor {
	
	private String name;
	
	private int currentWork;
	
	private int totalWork;
	
	
	public SimpleProgressMonitor(String name) {
		this.name = name;
	}

	
	@Override
    public void beginTask(String label, int totalWork) {
		println("Beginning task " + label + " (" + totalWork + ")");
		this.totalWork = totalWork;
		this.currentWork = 0;
	}

	
	@Override
    public void done() {
		println("Done");
	}

	
	@Override
    public boolean isCanceled() {
		return false;
	}
	
	
	protected void println(String text) {
		System.out.println(name + ": " + text);
	}

	
	@Override
	public void setCanceled(boolean value) {
	}


	@Override
	public void setTaskName(String value) {
		println("Task name: " + value);
	}


	@Override
    public void subTask(String label) {
		println("Subtask: " + label);
	}

	
	@Override
    public void worked(int amount) {
		currentWork += amount;
		println("Worked " + currentWork + " / " + totalWork);
	}
}
