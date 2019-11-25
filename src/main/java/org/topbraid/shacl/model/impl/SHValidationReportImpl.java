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

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.model.SHResult;
import org.topbraid.shacl.model.SHValidationReport;
import org.topbraid.shacl.vocabulary.SH;

public class SHValidationReportImpl extends SHResourceImpl implements SHValidationReport {
	
	public SHValidationReportImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	@Override
	public Iterable<SHResult> getResults() {
		List<SHResult> results = new LinkedList<>();
		for(Resource r : JenaUtil.getResourceProperties(this, SH.result)) {
			results.add(r.as(SHResult.class));
		}
		return results;
	}

	@Override
	public boolean isConformant() {
		return hasProperty(SH.conforms, JenaDatatypes.TRUE);
	}
	
}