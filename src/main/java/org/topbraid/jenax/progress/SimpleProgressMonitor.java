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

package org.topbraid.jenax.progress;


/**
 * A simple implementation of ProgressMonitor that prints messages to System.out.
 *
 * @author Holger Knublauch
 */
public class SimpleProgressMonitor implements ProgressMonitor {
	
	private volatile boolean canceled;
	
	private int currentWork;
	
	private String name;
	
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
		return canceled;
	}
	
	
	protected void println(String text) {
		System.out.println(name + ": " + text);
	}

	
	@Override
	public void setCanceled(boolean value) {
		this.canceled = value;
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
