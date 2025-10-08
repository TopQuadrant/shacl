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
package org.topbraid.shacl;

import eu.neverblink.jelly.convert.jena.riot.JellyLanguage;
import org.apache.jena.util.FileUtils;
import org.junit.Test;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.tools.Infer;
import org.topbraid.shacl.tools.Validate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * End-to-end tests for the CLI tools that use Jelly as the input and output format.
 *
 * @author Piotr Sowiński
 */
public class TestJelly {

    @Test
    public void testJellyInfer() throws Exception {
        String dataFile = this.getClass().getResource("/jelly/infer-data.jelly").getFile();
        String ruleFile = this.getClass().getResource("/jelly/infer-rule.jelly").getFile();

        // Redirect System.out to capture the output
        var oldOut = System.out;
        try {
            // Run the rule with Turtle output format
            var turtleOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(turtleOut));
            Infer.main(new String[]{
                    "-datafile", dataFile,
                    "-shapesfile", ruleFile,
                    "-outputFormat", "ttl"
            });

            // And with Jelly output
            var jellyOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(jellyOut));
            Infer.main(new String[]{
                    "-datafile", dataFile,
                    "-shapesfile", ruleFile,
                    "-outputFormat", "jelly"
            });

            assert(turtleOut.size() > 0);
            assert(jellyOut.size() > 0);

            // Check that the output contains the same number of triples
            var turtleModel = JenaUtil.createDefaultModel().read(
                new ByteArrayInputStream(turtleOut.toByteArray()), null, FileUtils.langTurtle);
            var jellyModel = JenaUtil.createDefaultModel().read(
                new ByteArrayInputStream(jellyOut.toByteArray()), null, JellyLanguage.JELLY.getName());
            assert(turtleModel.size() == jellyModel.size());
        } finally {
            System.setOut(oldOut);
        }
    }

    @Test
    public void testJellyValidate() throws Exception {
        String dataFile = this.getClass().getResource("/jelly/validate-data.jelly").getFile();
        String shapeFile = this.getClass().getResource("/jelly/validate-shape.jelly").getFile();

        // Redirect System.out to capture the output
        var oldOut = System.out;
        try {
            // Run the validation with Turtle output format
            var turtleOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(turtleOut));
            Validate.main(new String[]{
                    "-datafile", dataFile,
                    "-shapesfile", shapeFile,
                    "-outputFormat", "ttl"
            });

            // Run the validation with Jelly output
            var jellyOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(jellyOut));
            Validate.main(new String[]{
                    "-datafile", dataFile,
                    "-shapesfile", shapeFile,
                    "-outputFormat", "jelly"
            });

            assert(turtleOut.size() > 0);
            assert(jellyOut.size() > 0);

            // Check that the output contains the same number of triples
            var turtleModel = JenaUtil.createDefaultModel().read(
                new ByteArrayInputStream(turtleOut.toByteArray()), null, FileUtils.langTurtle);
            var jellyModel = JenaUtil.createDefaultModel().read(
                new ByteArrayInputStream(jellyOut.toByteArray()), null, JellyLanguage.JELLY.getName());
            assert(turtleModel.size() == jellyModel.size());
        } finally {
            System.setOut(oldOut);
        }
    }
}
