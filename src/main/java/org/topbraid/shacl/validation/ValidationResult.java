package org.topbraid.shacl.validation;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.Collection;
import java.util.List;

/**
 * A validation result, as produced by the validation engine.
 *
 * @author Holger Knublauch
 */
public interface ValidationResult {

    /**
     * See sh:focusNode.
     */
    RDFNode getFocusNode();

    /**
     * Gets the human-readable message attached to the result.
     * Note that validation results can have multiple messages in different languages, <code>getMessages()</code> might be
     * a better choice.
     *
     * @return a message or null
     */
    String getMessage();

    /**
     * Gets the human-readable message attached to the result (see sh:resultMessage).
     */
    Collection<RDFNode> getMessages();

    /**
     * See sh:resultPath.
     */
    Resource getPath();

    /**
     * Provides access to other RDF values that may exist for the result instance, for any given property.
     *
     * @param predicate the property to get the values of
     * @return the values, often empty
     */
    List<RDFNode> getPropertyValues(Property predicate);

    /**
     * See sh:resultSeverity.
     */
    Resource getSeverity();

    /**
     * See sh:sourceConstraint.
     */
    Resource getSourceConstraint();

    /**
     * See sh:sourceConstraintComponent.
     */
    Resource getSourceConstraintComponent();

    /**
     * See sh:sourceShape.
     */
    Resource getSourceShape();

    /**
     * See sh:value.
     */
    RDFNode getValue();
}
