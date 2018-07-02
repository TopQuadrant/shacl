package org.topbraid.shacl.validation;

/**
 * Configures the behaviour of the validation engine
 */
public class ValidationEngineConfiguration {

    // By default collect all possible errors
    private int validationErrorBatch = -1;

    // By default validate shapes
    private boolean validateShapes = true;

    /**
     * Maximum number of validations before returning the report
     * @return number of validations or -1 to mean all validations
     */
    public int getValidationErrorBatch() {
        return validationErrorBatch;
    }

    /**
     * Set the maximum number of validations before returning the rpoert
     * @param validationErrorBatch maximum number of validations or -1 for all validations
     * @return  current configuration after modification
     */
    public ValidationEngineConfiguration setValidationErrorBatch(int validationErrorBatch) {
        this.validationErrorBatch = validationErrorBatch;
        return this;
    }

    /**
     * Should the engine validates shapes
     * @return boolean flag for shapes validation
     */
    public boolean getValidateShapes() { return validateShapes; }

    /**
     * Sets an option for the engine to validate shapes
     * @param validateShapes boolean flat indicating if shapes must be validated
     * @return current configuration after modification
     */
    public ValidationEngineConfiguration setValidateShapes(boolean validateShapes) {
        this.validateShapes = validateShapes;
        return this;
    }
}
