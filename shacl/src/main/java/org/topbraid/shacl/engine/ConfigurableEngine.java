package org.topbraid.shacl.engine;

import org.topbraid.shacl.validation.ValidationEngineConfiguration;

public interface ConfigurableEngine {
    public ValidationEngineConfiguration getConfiguration();
    public void setConfiguration(ValidationEngineConfiguration configuration);
}
