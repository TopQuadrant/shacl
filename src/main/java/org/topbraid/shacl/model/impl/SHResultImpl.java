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
package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.model.SHResult;
import org.topbraid.shacl.vocabulary.SH;

public class SHResultImpl extends SHResourceImpl implements SHResult {
	
	public SHResultImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public RDFNode getFocusNode() {
		return JenaUtil.getProperty(this, SH.focusNode);
	}

	
	@Override
	public String getMessage() {
		return JenaUtil.getStringProperty(this, SH.resultMessage);
	}

	
	@Override
	public Resource getPath() {
		return getPropertyResourceValue(SH.resultPath);
	}

	
	@Override
	public Resource getSeverity() {
		return getPropertyResourceValue(SH.resultSeverity);
	}

	
	@Override
	public Resource getSourceConstraint() {
		return getPropertyResourceValue(SH.sourceConstraint);
	}

	
	@Override
	public Resource getSourceConstraintComponent() {
		return getPropertyResourceValue(SH.sourceConstraintComponent);
	}

	
	@Override
	public Resource getSourceShape() {
		return getPropertyResourceValue(SH.sourceShape);
	}

	
	@Override
	public RDFNode getValue() {
		return JenaUtil.getProperty(this, SH.value);
	}
}
