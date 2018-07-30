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
package org.topbraid.shacl.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary extension from http://www.w3.org/ns/shacl#
 */
public class SHEXT {

    public final static String BASE_URI = "http://www.w3.org/ns/shacl/report/extended#";

    public final static String NS = BASE_URI;

    public final static String PREFIX = "sh";

    public final static Property targetCount = ResourceFactory.createProperty(NS + "targetCount");

    public static String getURI() {
        return NS;
    }
}
