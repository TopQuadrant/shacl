package org.topbraid.shacl;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.junit.Test;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.jenax.util.SystemTriples;
import org.topbraid.shacl.util.SHACLSystemModel;
import org.topbraid.shacl.validation.ValidationEngineConfiguration;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;

public class TestValidatorConfiguration {

    @Test
    public void testMaxErrors() {
        Model dataModel = JenaUtil.createMemoryModel();
        dataModel.read(ValidationExample.class.getResourceAsStream("/sh/tests/core/property/class-001.test.ttl"), "urn:dummy", FileUtils.langTurtle);
        dataModel.add(SystemTriples.getVocabularyModel());
        dataModel.add(SHACLSystemModel.getSHACLModel());

        ValidationEngineConfiguration configuration = new ValidationEngineConfiguration();
        configuration.setValidationErrorBatch(-1);

        Resource reportNoMaximum = ValidationUtil.validateModel(dataModel, dataModel, configuration);

        Model resultModel = reportNoMaximum.getModel();
        
        assert(resultModel.listStatements(null, SH.resultSeverity, SH.Violation).toList().size() == 2);

        configuration.setValidationErrorBatch(1);
        Resource reportMaximum = ValidationUtil.validateModel(dataModel, dataModel, configuration);

        resultModel = reportMaximum.getModel();
        assert(resultModel.listStatements(null, SH.resultSeverity, SH.Violation).toList().size() == 1);
    }
}
