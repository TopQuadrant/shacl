package org.topbraid.shacl.validation.predicates;

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
public class MinSeverityShapePredicate implements Predicate<SHShape> {
	
	private Integer minSeverityScore;
	
	
	public MinSeverityShapePredicate(Resource minSeverity) {
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
