package org.topbraid.shacl.validation;

import org.apache.jena.rdf.model.Resource;

import java.util.HashSet;
import java.util.Set;

/**
 * Configures the behavior of the validation engine.
 */
public class ValidationEngineConfiguration {

    // By default don't produce sh:detail
    private boolean reportDetails = false;

    // By default validate all constraints
    private Set<Resource> skippedConstraintComponents = new HashSet<>();

    // By default validate shapes
    private boolean validateShapes = true;

    // By default collect all possible errors
    private int validationErrorBatch = -1;


    /**
     * Creates a clone of this, with exactly the same values.
     */
    @Override
    public ValidationEngineConfiguration clone() {
        ValidationEngineConfiguration c = new ValidationEngineConfiguration();
        c.reportDetails = this.reportDetails;
        c.skippedConstraintComponents = new HashSet<>(this.skippedConstraintComponents);
        c.validateShapes = this.validateShapes;
        c.validationErrorBatch = this.validationErrorBatch;
        return c;
    }


    /**
     * Checks whether the report shall include sh:detail triples (for sh:node etc).
     *
     * @return true to report details (false is default)
     */
    public boolean getReportDetails() {
        return reportDetails;
    }

    /**
     * Specifies whether the report shall include sh:detail triples where supported.
     *
     * @param reportDetails true to produce sh:details, false for the default
     * @return current configuration after modification
     */
    public ValidationEngineConfiguration setReportDetails(boolean reportDetails) {
        this.reportDetails = reportDetails;
        return this;
    }

    /**
     * Maximum number of validations before returning the report.
     *
     * @return number of validations or -1 to mean all validations
     */
    public int getValidationErrorBatch() {
        return validationErrorBatch;
    }

    /**
     * Set the maximum number of validations before returning the report.
     *
     * @param validationErrorBatch maximum number of validations or -1 for all validations
     * @return current configuration after modification
     */
    public ValidationEngineConfiguration setValidationErrorBatch(int validationErrorBatch) {
        this.validationErrorBatch = validationErrorBatch;
        return this;
    }

    /**
     * Should the engine validates shapes
     *
     * @return boolean flag for shapes validation
     */
    public boolean getValidateShapes() {
        return validateShapes;
    }

    /**
     * Sets an option for the engine to validate shapes
     *
     * @param validateShapes boolean flat indicating if shapes must be validated
     * @return current configuration after modification
     */
    public ValidationEngineConfiguration setValidateShapes(boolean validateShapes) {
        this.validateShapes = validateShapes;
        return this;
    }

    /**
     * Checks whether the engine should skip constraints of a given constraint component.
     *
     * @param component the constraint component
     * @return true  if component shall be skipped
     */
    public boolean isSkippedConstraintComponent(Resource component) {
        return skippedConstraintComponents.contains(component);
    }

    public void addSkippedConstraintComponent(Resource component) {
        skippedConstraintComponents.add(component);
    }
}
