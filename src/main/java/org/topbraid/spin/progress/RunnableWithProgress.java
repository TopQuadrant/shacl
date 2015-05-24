/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.progress;


/**
 * A generic interface similar to Runnable, but with an additional
 * argument that allows the Runnable to display progress.
 *
 * @author Holger Knublauch
 */
public interface RunnableWithProgress {

	/**
	 * Runs the runnable.
	 * @param monitor  an optional ProgressMonitor
	 */
	void run(ProgressMonitor monitor);
}
