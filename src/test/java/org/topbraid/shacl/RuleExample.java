package org.topbraid.shacl;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileUtils;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.rules.RuleUtil;
import org.topbraid.shacl.util.ModelPrinter;

public class RuleExample {

    /**
     * Loads an example SHACL-AF (rules) file and execute it against the data.
     */
    public static void main(String[] args) throws Exception {

        // Load the main data model that contains rule(s)
        Model dataModel = JenaUtil.createMemoryModel();
        dataModel.read(RuleExample.class.getResourceAsStream("sh/tests/rules/triple/rectangle.test.ttl"), "urn:dummy",
                FileUtils.langTurtle);

        // Perform the rule calculation, using the data model
        // also as the rule model - you may have them separated
        Model result = RuleUtil.executeRules(dataModel, dataModel, null, null);

        // you may want to add the original data, to make sense of the rule results
        result.add(dataModel);

        // Print rule calculation results
        System.out.println(ModelPrinter.get().print(result));
    }
}
