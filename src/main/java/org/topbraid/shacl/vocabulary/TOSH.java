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
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary for <a href="http://topbraid.org/tosh">TOSH</a>
 */
public class TOSH {

    public final static String BASE_URI = "http://topbraid.org/tosh";

    public final static String NAME = "TopBraid Data Shapes Vocabulary";

    public final static String NS = BASE_URI + "#";

    public final static String PREFIX = "tosh";


    public final static Resource count = ResourceFactory.createResource(NS + "count");

    public final static Resource DatatypePropertyShapeView = ResourceFactory.createResource(NS + "DatatypePropertyShapeView");

    public final static Resource DeleteTripleSuggestionGenerator = ResourceFactory.createResource(NS + "DeleteTripleSuggestionGenerator");

    public final static Resource NodeProcessor = ResourceFactory.createResource(NS + "NodeProcessor");

    public final static Resource NodeShapeConstraintsShape = ResourceFactory.createResource(NS + "NodeShapeConstraintsShape");

    public final static Resource ObjectPropertyShapeView = ResourceFactory.createResource(NS + "ObjectPropertyShapeView");

    public final static Resource ResultsGenerators = ResourceFactory.createResource(NS + "ResultsGenerators");

    public final static Resource TeamworkPlatform = ResourceFactory.createResource(NS + "TeamworkPlatform");

    public final static Resource TopBraidPlatform = ResourceFactory.createResource(NS + "TopBraidPlatform");

    public final static Resource evalExpr = ResourceFactory.createResource(NS + "evalExpr");

    public final static Resource hasShape = ResourceFactory.createResource(NS + "hasShape");

    public final static Resource isInTargetOf = ResourceFactory.createResource(NS + "isInTargetOf");

    public final static Resource targetContains = ResourceFactory.createResource(NS + "targetContains");

    public final static Resource values = ResourceFactory.createResource(NS + "values");


    public final static Property closed = ResourceFactory.createProperty(NS + "closed");

    public final static Property editGroupDescription = ResourceFactory.createProperty(NS + "editGroupDescription");

    public final static Property javaMethod = ResourceFactory.createProperty(NS + "javaMethod");

    public final static Property useDeclaredDatatype = ResourceFactory.createProperty(NS + "useDeclaredDatatype");

    // Note this property may be deleted in future versions
    public final static Property viewGadget = ResourceFactory.createProperty(NS + "viewGadget");

    public final static Property viewGroupDescription = ResourceFactory.createProperty(NS + "viewGroupDescription");

    public final static Property viewWidget = ResourceFactory.createProperty(NS + "viewWidget");


    public static String getURI() {
        return NS;
    }
}
