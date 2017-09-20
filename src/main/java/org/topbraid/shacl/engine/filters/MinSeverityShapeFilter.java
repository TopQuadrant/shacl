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
package org.topbraid.shacl.engine.filters;

import java.util.function.Predicate;

import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHShape;
import org.topbraid.shacl.vocabulary.SH;

/**
 * A Predicate that can be used to bypass any shapes that have less than a minimum severity,
 * in the order of sh:Violation, sh:Warning and any other.
 * 
 * @author Holger Knublauch
 */
public class MinSeverityShapeFilter implements Predicate<SHShape> {
	
	private Integer minSeverityScore;
	
	
	public MinSeverityShapeFilter(Resource minSeverity) {
		this.minSeverityScore = getScore(minSeverity);
	}
	
	
	private Integer getScore(Resource severity) {
		if(SH.Violation.equals(severity)) {
			return 2;
		}
		else if(SH.Warning.equals(severity)) {
			return 1;
		}
		else {
			return 0;
		}
	}

	
	@Override
	public boolean test(SHShape shape) {
		Resource severity = shape.getSeverity();
		Integer score = getScore(severity);
		return score.compareTo(minSeverityScore) >= 0;
	}
}
