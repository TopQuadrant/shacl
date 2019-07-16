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
