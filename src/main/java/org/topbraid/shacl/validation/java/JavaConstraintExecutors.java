package org.topbraid.shacl.validation.java;

import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.ConstraintExecutor;
import org.topbraid.shacl.validation.ConstraintExecutors;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class JavaConstraintExecutors {

    private static Map<Resource, Function<Constraint, ConstraintExecutor>> map = new HashMap<>();

    static {
        map.put(SH.AndConstraintComponent, AndConstraintExecutor::new);
        map.put(SH.ClassConstraintComponent, constraint -> new ClassConstraintExecutor());
        map.put(SH.ClosedConstraintComponent, ClosedConstraintExecutor::new);
        map.put(SH.DatatypeConstraintComponent, constraint -> new DatatypeConstraintExecutor());
        map.put(SH.DisjointConstraintComponent, constraint -> new DisjointConstraintExecutor());
        map.put(SH.EqualsConstraintComponent, constraint -> new EqualsConstraintExecutor());
        map.put(SH.HasValueConstraintComponent, constraint -> new HasValueConstraintExecutor());
        map.put(SH.InConstraintComponent, InConstraintExecutor::new);
        map.put(SH.LanguageInConstraintComponent, LanguageInConstraintExecutor::new);
        map.put(SH.LessThanConstraintComponent, constraint -> new LessThanConstraintExecutor());
        map.put(SH.LessThanOrEqualsConstraintComponent, constraint -> new LessThanOrEqualsConstraintExecutor());
        map.put(SH.MaxCountConstraintComponent, MaxCountConstraintExecutor::new);
        map.put(SH.MaxExclusiveConstraintComponent, constraint -> new MaxExclusiveConstraintExecutor());
        map.put(SH.MaxInclusiveConstraintComponent, constraint -> new MaxInclusiveConstraintExecutor());
        map.put(SH.MaxLengthConstraintComponent, MaxLengthConstraintExecutor::new);
        map.put(SH.MinCountConstraintComponent, MinCountConstraintExecutor::new);
        map.put(SH.MinExclusiveConstraintComponent, constraint -> new MinExclusiveConstraintExecutor());
        map.put(SH.MinInclusiveConstraintComponent, constraint -> new MinInclusiveConstraintExecutor());
        map.put(SH.MinLengthConstraintComponent, MinLengthConstraintExecutor::new);
        map.put(SH.NodeConstraintComponent, constraint -> new NodeConstraintExecutor());
        map.put(SH.NodeKindConstraintComponent, constraint -> new NodeKindConstraintExecutor());
        map.put(SH.NotConstraintComponent, constraint -> new NotConstraintExecutor());
        map.put(SH.OrConstraintComponent, OrConstraintExecutor::new);
        map.put(SH.PatternConstraintComponent, PatternConstraintExecutor::new);
        map.put(SH.QualifiedMaxCountConstraintComponent, QualifiedValueShapeConstraintExecutor::new);
        map.put(SH.QualifiedMinCountConstraintComponent, QualifiedValueShapeConstraintExecutor::new);
        map.put(SH.UniqueLangConstraintComponent, constraint -> new UniqueLangConstraintExecutor());
        map.put(SH.XoneConstraintComponent, XoneConstraintExecutor::new);

        map.put(DASH.SingleLineConstraintComponent, constraint -> new SingleLineConstraintExecutor());
    }


    public static void install(ConstraintExecutors constraintExecutors) {
        for (Resource cc : map.keySet()) {
            constraintExecutors.addSpecialExecutor(cc, map.get(cc));
        }
    }
}
