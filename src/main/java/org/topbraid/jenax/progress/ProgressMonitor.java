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
 * Inspired by the Eclipse IProgressMonitor, this interface supports monitoring long-running processes with intermediate
 * status messages and the ability to cancel.
 *
 * @author Holger Knublauch
 */
public interface ProgressMonitor {

    /**
     * Typically used by the (long-running) process to determine whether the user has requested cancellation.
     * The process should then find a suitable, clean termination.
     *
     * @return true  if cancel was requested
     */
    boolean isCanceled();


    /**
     * Informs the progress monitor that a new task has been started, with a given number of expected steps.
     * A UI connected to the ProgressMonitor would typically display something like a progress bar and the task name.
     *
     * @param label     the name of the task
     * @param totalWork the number of steps (see <code>worked</code>) that is expected to be needed to complete the task
     */
    void beginTask(String label, int totalWork);


    /**
     * Informs the progress monitor that all is completed.
     */
    void done();


    /**
     * Typically called from a parallel thread triggered by the user, this informs the progress monitor that it needs to
     * return <code>true</code> for <code>isCanceled</code>.
     * Once a process has been canceled, it should not be un-canceled.
     *
     * @param value true if canceled
     */
    void setCanceled(boolean value);


    /**
     * Changes the name or label of the current task.
     *
     * @param value the task name
     */
    void setTaskName(String value);


    /**
     * Sets the label that serves as sub-task, typically printed under the main task name.
     *
     * @param label the subtask label
     */
    void subTask(String label);


    /**
     * Informs the progress monitor that one or more steps have been completed towards the current task (see <code>beginTask</code>).
     *
     * @param amount the number of steps completed
     */
    void worked(int amount);
}
