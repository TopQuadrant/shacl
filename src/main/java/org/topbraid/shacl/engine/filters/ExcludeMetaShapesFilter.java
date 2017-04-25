package org.topbraid.shacl.engine.filters;

import java.util.function.Predicate;

import org.topbraid.shacl.model.SHShape;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

/**
 * A Predicate that can be used to bypass any shapes that are also constraint components.
 * 
 * @author Holger Knublauch
 */
public class ExcludeMetaShapesFilter implements Predicate<SHShape> {

	@Override
	public boolean test(SHShape shape) {
		return !JenaUtil.hasIndirectType(shape, SH.ConstraintComponent);
	}
}
