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
package org.topbraid.jenax.util;

/**
 * An Exception thrown if a named graph could not be resolved
 * while setting the default graph of a dataset.
 * <p>
 * This is subclassing RuntimeException because otherwise a lot of
 * existing code would have to catch GraphNotFoundException
 * (where it would otherwise have crashed with a NullPointerException anyway).
 *
 * @author Holger Knublauch
 */
public class GraphNotFoundException extends RuntimeException {

    public GraphNotFoundException(String message) {
        super(message);
    }
}
