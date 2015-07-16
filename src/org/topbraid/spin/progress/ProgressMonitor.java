/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.progress;


/**
 * An abstraction of the Eclipse IProgressMonitor for intermediate
 * status messages and the ability to cancel long-running processes.
 * 
 * @author Holger Knublauch
 */
public interface ProgressMonitor {

	boolean isCanceled();
	
	
	void beginTask(String label, int totalWork);
	
	
	void done();
	
	
	void setCanceled(boolean value);
	
	
	void setTaskName(String value);
	
	
	void subTask(String label);
	
	
	void worked(int amount);
}
