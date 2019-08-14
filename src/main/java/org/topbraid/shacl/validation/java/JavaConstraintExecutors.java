package org.topbraid.shacl.validation.java;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.ConstraintExecutor;
import org.topbraid.shacl.validation.ConstraintExecutors;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

public class JavaConstraintExecutors {
	
	private static Map<Resource,Function<Constraint,ConstraintExecutor>> map = new HashMap<>();
	static {
		map.put(SH.AndConstraintComponent, constraint -> new AndConstraintExecutor(constraint));
		map.put(SH.ClassConstraintComponent, constraint -> new ClassConstraintExecutor());
		map.put(SH.ClosedConstraintComponent, constraint -> new ClosedConstraintExecutor(constraint));
		map.put(SH.DatatypeConstraintComponent, constraint -> new DatatypeConstraintExecutor());
		map.put(SH.DisjointConstraintComponent, constraint -> new DisjointConstraintExecutor());
		map.put(SH.EqualsConstraintComponent, constraint -> new EqualsConstraintExecutor());
		map.put(SH.HasValueConstraintComponent, constraint -> new HasValueConstraintExecutor());
		map.put(SH.InConstraintComponent, constraint -> new InConstraintExecutor(constraint));
		map.put(SH.LanguageInConstraintComponent, constraint -> new LanguageInConstraintExecutor(constraint));
		map.put(SH.LessThanConstraintComponent, constraint -> new LessThanConstraintExecutor());
		map.put(SH.LessThanOrEqualsConstraintComponent, constraint -> new LessThanOrEqualsConstraintExecutor());
		map.put(SH.MaxCountConstraintComponent, constraint -> new MaxCountConstraintExecutor(constraint));
		map.put(SH.MaxExclusiveConstraintComponent, constraint -> new MaxExclusiveConstraintExecutor());
		map.put(SH.MaxInclusiveConstraintComponent, constraint -> new MaxInclusiveConstraintExecutor());
		map.put(SH.MaxLengthConstraintComponent, constraint -> new MaxLengthConstraintExecutor(constraint));
		map.put(SH.MinCountConstraintComponent, constraint -> new MinCountConstraintExecutor(constraint));
		map.put(SH.MinExclusiveConstraintComponent, constraint -> new MinExclusiveConstraintExecutor());
		map.put(SH.MinInclusiveConstraintComponent, constraint -> new MinInclusiveConstraintExecutor());
		map.put(SH.MinLengthConstraintComponent, constraint -> new MinLengthConstraintExecutor(constraint));
		map.put(SH.NodeConstraintComponent, constraint -> new NodeConstraintExecutor());
		map.put(SH.NodeKindConstraintComponent, constraint -> new NodeKindConstraintExecutor());
		map.put(SH.NotConstraintComponent, constraint -> new NotConstraintExecutor());
		map.put(SH.OrConstraintComponent, constraint -> new OrConstraintExecutor(constraint));
		map.put(SH.PatternConstraintComponent, constraint -> new PatternConstraintExecutor(constraint));
		map.put(SH.QualifiedMaxCountConstraintComponent, constraint -> new QualifiedValueShapeConstraintExecutor(constraint));
		map.put(SH.QualifiedMinCountConstraintComponent, constraint -> new QualifiedValueShapeConstraintExecutor(constraint));
		map.put(SH.UniqueLangConstraintComponent, constraint -> new UniqueLangConstraintExecutor());
		map.put(SH.XoneConstraintComponent, constraint -> new XoneConstraintExecutor(constraint));
		
		map.put(DASH.SingleLineConstraintComponent, constraint -> new SingleLineConstraintExecutor());
	}

	
	public static void install(ConstraintExecutors constraintExecutors) {
		for(Resource cc : map.keySet()) {
			constraintExecutors.addSpecialExecutor(cc, map.get(cc));
		}
	}
}
