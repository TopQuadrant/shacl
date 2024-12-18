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
package org.topbraid.shacl.expr;

import org.apache.jena.rdf.model.RDFNode;

import java.util.Iterator;
import java.util.List;

public abstract class ComplexNodeExpression extends AbstractNodeExpression {

    protected ComplexNodeExpression(RDFNode expr) {
        super(expr);
    }


    @Override
    public String getFunctionalSyntax() {
        String str = getFunctionalSyntaxName();
        str += "(";
        List<String> args = getFunctionalSyntaxArguments();
        Iterator<String> it = args.iterator();
        while (it.hasNext()) {
            String next = it.next();
            str += next;
            if (it.hasNext()) {
                str += ", ";
            }
        }
        str += ")";
        return str;
    }


    protected String getFunctionalSyntaxName() {
        return getTypeId();
    }


    public abstract List<String> getFunctionalSyntaxArguments();
}
