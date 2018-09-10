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

import java.util.Iterator;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;

public abstract class ComplexNodeExpression extends AbstractNodeExpression {
	
	protected ComplexNodeExpression(RDFNode expr) {
		super(expr);
	}
	
	
	@Override
	public String getFunctionalSyntax() {
		String str = getTypeId() + "(";
		List<String> args = getFunctionalSyntaxArguments();
		Iterator<String> it = args.iterator();
		while(it.hasNext()) {
			String next = it.next();
			str += next;
			if(it.hasNext()) {
				str += ", ";
			}
		}
		return str + ")";
	}
	
	
	public abstract List<String> getFunctionalSyntaxArguments();



	public abstract void appendSPARQL(AppendContext context, String targetVarName);

	
	protected void appendSPARQL(AppendContext context, String targetVarName, NodeExpression expr) {
		if(expr instanceof AtomicNodeExpression) {
			context.append("BIND(" + expr.toString() + " AS ?" + targetVarName + ") .\n");
		}
		else {
			((ComplexNodeExpression)expr).appendSPARQL(context, targetVarName);
		}
	}
	
	
	public String getSPARQL() {
		StringBuffer sb = new StringBuffer();
		AppendContext context = new AppendContext(sb);
		context.append("{\n");
		context.increaseIndent();
		appendSPARQL(context, "result");
		context.decreaseIndent();
		context.append("}");
		return sb.toString();
	}
}
