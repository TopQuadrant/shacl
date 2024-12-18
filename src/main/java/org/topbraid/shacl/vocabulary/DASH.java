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
 * Vocabulary for <a href="http://datashapes.org/dash">DASH</a>
 */
public class DASH {

    public final static String BASE_URI = "http://datashapes.org/dash";

    public final static String NAME = "DASH Data Shapes Vocabulary";

    public final static String NS = BASE_URI + "#";

    public final static String PREFIX = "dash";


    public final static Resource Action = ResourceFactory.createResource(NS + "Action");

    public final static Resource ActionGroup = ResourceFactory.createResource(NS + "ActionGroup");

    public final static Resource ActionTestCase = ResourceFactory.createResource(NS + "ActionTestCase");

    public final static Resource addedGraph = ResourceFactory.createResource(NS + "addedGraph");

    public final static Resource all = ResourceFactory.createResource(NS + "all");

    public final static Resource ChangeScript = ResourceFactory.createResource(NS + "ChangeScript");

    public final static Resource CommitScript = ResourceFactory.createResource(NS + "CommitScript");

    public final static Resource Constructor = ResourceFactory.createResource(NS + "Constructor");

    public final static Resource deletedGraph = ResourceFactory.createResource(NS + "deletedGraph");

    public final static Resource DepictionRole = ResourceFactory.createResource(NS + "DepictionRole");

    public final static Resource Deprecated = ResourceFactory.createResource(NS + "Deprecated");

    public final static Resource DescriptionRole = ResourceFactory.createResource(NS + "DescriptionRole");

    public final static Resource Editor = ResourceFactory.createResource(NS + "Editor");

    public final static Resource ExecutionPlatform = ResourceFactory.createResource(NS + "ExecutionPlatform");

    public final static Resource Experimental = ResourceFactory.createResource(NS + "Experimental");

    public final static Resource ExploreAction = ResourceFactory.createResource(NS + "ExploreAction");

    public final static Resource FailureResult = ResourceFactory.createResource(NS + "FailureResult");

    public final static Resource FailureTestCaseResult = ResourceFactory.createResource(NS + "FailureTestCaseResult");

    public final static Resource FunctionTestCase = ResourceFactory.createResource(NS + "FunctionTestCase");

    public final static Resource GraphService = ResourceFactory.createResource(NS + "GraphService");

    public final static Resource GraphStoreTestCase = ResourceFactory.createResource(NS + "GraphStoreTestCase");

    public final static Resource GraphUpdate = ResourceFactory.createResource(NS + "GraphUpdate");

    public final static Resource GraphValidationTestCase = ResourceFactory.createResource(NS + "GraphValidationTestCase");

    public final static Resource IconRole = ResourceFactory.createResource(NS + "IconRole");

    public final static Resource IDRole = ResourceFactory.createResource(NS + "IDRole");

    public final static Resource IncludedScript = ResourceFactory.createResource(NS + "IncludedScript");

    public final static Resource IndexedConstraintComponent = ResourceFactory.createResource(NS + "IndexedConstraintComponent");

    public final static Resource InferencingTestCase = ResourceFactory.createResource(NS + "InferencingTestCase");

    public final static Resource isDeactivated = ResourceFactory.createResource(NS + "isDeactivated");

    public final static Resource JSTestCase = ResourceFactory.createResource(NS + "JSTestCase");

    public final static Resource KeyInfoRole = ResourceFactory.createResource(NS + "KeyInfoRole");

    public final static Resource LabelRole = ResourceFactory.createResource(NS + "LabelRole");

    public final static Resource ListShape = ResourceFactory.createResource(NS + "ListShape");

    public final static Resource ModifyAction = ResourceFactory.createResource(NS + "ModifyAction");

    public final static Resource MultiFunction = ResourceFactory.createResource(NS + "MultiFunction");

    public final static Resource None = ResourceFactory.createResource(NS + "None");

    public final static Resource NonRecursiveConstraintComponent = ResourceFactory.createResource(NS + "NonRecursiveConstraintComponent");

    public final static Resource QueryTestCase = ResourceFactory.createResource(NS + "QueryTestCase");

    public final static Resource ParameterConstraintComponent = ResourceFactory.createResource(NS + "ParameterConstraintComponent");

    public final static Resource RDFQueryJSLibrary = ResourceFactory.createResource(NS + "RDFQueryJSLibrary");

    public final static Resource ReifiableByConstraintComponent = ResourceFactory.createResource(NS + "ReifiableByConstraintComponent");

    public final static Resource ResourceAction = ResourceFactory.createResource(NS + "ResourceAction");

    public final static Resource ResourceService = ResourceFactory.createResource(NS + "ResourceService");

    public final static Resource Script = ResourceFactory.createResource(NS + "Script");

    public final static Resource ScriptConstraint = ResourceFactory.createResource(NS + "ScriptConstraint");

    public final static Resource ScriptConstraintComponent = ResourceFactory.createResource(NS + "ScriptConstraintComponent");

    public final static Resource ScriptFunction = ResourceFactory.createResource(NS + "ScriptFunction");

    public final static Resource ScriptSuggestionGenerator = ResourceFactory.createResource(NS + "ScriptSuggestionGenerator");

    public final static Resource ScriptTestCase = ResourceFactory.createResource(NS + "ScriptTestCase");

    public final static Resource ScriptValidator = ResourceFactory.createResource(NS + "ScriptValidator");

    public final static Resource Service = ResourceFactory.createResource(NS + "Service");

    public final static Resource ShapeClass = ResourceFactory.createResource(NS + "ShapeClass");

    public final static Resource ShapeScript = ResourceFactory.createResource(NS + "ShapeScript");

    public final static Resource SPARQLConstructTemplate = ResourceFactory.createResource(NS + "SPARQLConstructTemplate");

    public final static Resource SPARQLMultiFunction = ResourceFactory.createResource(NS + "SPARQLMultiFunction");

    public final static Resource SPARQLSelectTemplate = ResourceFactory.createResource(NS + "SPARQLSelectTemplate");

    public final static Resource SPARQLUpdateSuggestionGenerator = ResourceFactory.createResource(NS + "SPARQLUpdateSuggestionGenerator");

    public final static Resource SingleLineConstraintComponent = ResourceFactory.createResource(NS + "SingleLineConstraintComponent");

    public final static Resource Stable = ResourceFactory.createResource(NS + "Stable");

    public final static Resource SuccessResult = ResourceFactory.createResource(NS + "SuccessResult");

    public final static Resource SuccessTestCaseResult = ResourceFactory.createResource(NS + "SuccessTestCaseResult");

    public final static Resource SuggestionResult = ResourceFactory.createResource(NS + "SuggestionResult");

    public final static Resource TestCase = ResourceFactory.createResource(NS + "TestCase");

    public final static Resource ValidationTestCase = ResourceFactory.createResource(NS + "ValidationTestCase");

    public final static Resource ValueTableViewer = ResourceFactory.createResource(NS + "ValueTableViewer");

    public final static Resource Viewer = ResourceFactory.createResource(NS + "Viewer");

    public final static Resource Widget = ResourceFactory.createResource(NS + "Widget");


    public final static Property abstract_ = ResourceFactory.createProperty(NS + "abstract");

    public final static Property action = ResourceFactory.createProperty(NS + "action");

    public final static Property actualResult = ResourceFactory.createProperty(NS + "actualResult");

    public final static Property actionGroup = ResourceFactory.createProperty(NS + "actionGroup");

    public final static Property addedTriple = ResourceFactory.createProperty(NS + "addedTriple");

    public final static Property apiStatus = ResourceFactory.createProperty(NS + "apiStatus");

    public final static Property applicableToClass = ResourceFactory.createProperty(NS + "applicableToClass");

    public final static Property cachable = ResourceFactory.createProperty(NS + "cachable");

    public final static Property canWrite = ResourceFactory.createProperty(NS + "canWrite");

    public final static Property composite = ResourceFactory.createProperty(NS + "composite");

    public final static Property constructor = ResourceFactory.createProperty(NS + "constructor");

    public final static Property contextFree = ResourceFactory.createProperty(NS + "contextFree");

    public final static Property defaultViewForRole = ResourceFactory.createProperty(NS + "defaultViewForRole");

    public final static Property deletedTriple = ResourceFactory.createProperty(NS + "deletedTriple");

    public final static Property dependencyPredicate = ResourceFactory.createProperty(NS + "dependencyPredicate");

    public final static Property detailsEndpoint = ResourceFactory.createProperty(NS + "detailsEndpoint");

    public final static Property detailsGraph = ResourceFactory.createProperty(NS + "detailsGraph");

    public final static Property editor = ResourceFactory.createProperty(NS + "editor");

    public final static Property expectedResult = ResourceFactory.createProperty(NS + "expectedResult");

    public final static Property expectedResultIsJSON = ResourceFactory.createProperty(NS + "expectedResultIsJSON");

    public final static Property expectedResultIsTTL = ResourceFactory.createProperty(NS + "expectedResultIsTTL");

    public final static Property exports = ResourceFactory.createProperty(NS + "exports");

    public final static Property expression = ResourceFactory.createProperty(NS + "expression");

    public final static Property focusNode = ResourceFactory.createProperty(NS + "focusNode");

    public final static Property generateClass = ResourceFactory.createProperty(NS + "generateClass");

    public final static Property generatePrefixClasses = ResourceFactory.createProperty(NS + "generatePrefixClasses");

    public final static Property generatePrefixConstants = ResourceFactory.createProperty(NS + "generatePrefixConstants");

    public final static Property hidden = ResourceFactory.createProperty(NS + "hidden");

    public final static Property includedExecutionPlatform = ResourceFactory.createProperty(NS + "includedExecutionPlatform");

    public final static Property includeSuggestions = ResourceFactory.createProperty(NS + "includeSuggestions");

    public final static Property index = ResourceFactory.createProperty(NS + "index");

    public final static Property indexed = ResourceFactory.createProperty(NS + "indexed");

    public final static Property js = ResourceFactory.createProperty(NS + "js");

    public final static Property jsCondition = ResourceFactory.createProperty(NS + "jsCondition");

    public final static Property mimeTypes = ResourceFactory.createProperty(NS + "mimeTypes");

    public final static Property neverMaterialize = ResourceFactory.createProperty(NS + "neverMaterialize");

    public final static Property node = ResourceFactory.createProperty(NS + "node");

    public final static Property onAllValues = ResourceFactory.createProperty(NS + "onAllValues");

    public final static Property private_ = ResourceFactory.createProperty(NS + "private");

    public final static Property propertyRole = ResourceFactory.createProperty(NS + "propertyRole");

    public final static Property propertySuggestionGenerator = ResourceFactory.createProperty(NS + "propertySuggestionGenerator");

    public final static Property requiredExecutionPlatform = ResourceFactory.createProperty(NS + "requiredExecutionPlatform");

    public final static Property resultVariable = ResourceFactory.createProperty(NS + "resultVariable");

    public final static Property rootClass = ResourceFactory.createProperty(NS + "rootClass");

    public final static Property readOnly = ResourceFactory.createProperty(NS + "readOnly");

    public final static Property reifiableBy = ResourceFactory.createProperty(NS + "reifiableBy");

    public final static Property resourceAction = ResourceFactory.createProperty(NS + "resourceAction");

    public final static Property resourceService = ResourceFactory.createProperty(NS + "resourceService");

    public final static Property responseContentType = ResourceFactory.createProperty(NS + "responseContentType");

    public final static Property scriptConstraint = ResourceFactory.createProperty(NS + "scriptConstraint");

    public final static Property shape = ResourceFactory.createProperty(NS + "shape");

    public final static Property shapeScript = ResourceFactory.createProperty(NS + "shapeScript");

    public final static Property singleLine = ResourceFactory.createProperty(NS + "singleLine");

    public final static Property suggestion = ResourceFactory.createProperty(NS + "suggestion");

    public final static Property suggestionConfidence = ResourceFactory.createProperty(NS + "suggestionConfidence");

    public final static Property suggestionGenerator = ResourceFactory.createProperty(NS + "suggestionGenerator");

    public final static Property suggestionGroup = ResourceFactory.createProperty(NS + "suggestionGroup");

    public final static Property testCase = ResourceFactory.createProperty(NS + "testCase");

    public final static Property testGraph = ResourceFactory.createProperty(NS + "testGraph");

    public final static Property uri = ResourceFactory.createProperty(NS + "uri");

    public final static Property uriStart = ResourceFactory.createProperty(NS + "uriStart");

    public final static Property validateShapes = ResourceFactory.createProperty(NS + "validateShapes");

    public final static Property variables = ResourceFactory.createProperty(NS + "variables");

    public final static Property viewer = ResourceFactory.createProperty(NS + "viewer");

    public final static Property x = ResourceFactory.createProperty(NS + "x");

    public final static Property y = ResourceFactory.createProperty(NS + "y");


    public static String getURI() {
        return NS;
    }


    /**
     * Checks whether a given feature shall be included into the generated APIs.
     *
     * @param feature the feature to check
     * @return true currently if the feature has any value for dash:apiStatus but this may change in case we introduce
     * additional stati in the future.
     */
    public static boolean isAPI(Resource feature) {
        return feature.hasProperty(DASH.apiStatus);
    }
}
