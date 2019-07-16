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
package org.topbraid.shacl.model;

import org.apache.jena.rdf.model.Resource;

/**
 * The root interface of all resources of interest to SHACL.
 * 
 * This extends Jena's Resource but it should be easy to adapt to, say, RDF4J.
 * 
 * @author Holger Knublauch
 */
public interface SHResource extends Resource {
}
