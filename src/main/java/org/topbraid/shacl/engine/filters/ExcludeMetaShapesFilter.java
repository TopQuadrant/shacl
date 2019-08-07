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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.jena.rdf.model.Resource;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.model.SHShape;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.shacl.vocabulary.TOSH;

/**
 * A Predicate that can be used to bypass any shapes that are also constraint components.
 * 
 * @author Holger Knublauch
 */
public class ExcludeMetaShapesFilter implements Predicate<SHShape> {
	
	private static Set<Resource> systemShapes = new HashSet<>();
	static {
		Collections.addAll(systemShapes, TOSH.PropertyGroupShape, TOSH.PropertyShapeShape, TOSH.ShapeShape);
		Collections.addAll(systemShapes, DASH.Editor, DASH.GraphStoreTestCase, DASH.InferencingTestCase, DASH.QueryTestCase, DASH.ValidationTestCase, DASH.Viewer, DASH.Widget);
	}


	public static void addSystemShapes(Resource... shapes) {
		for(Resource shape : shapes) {
			systemShapes.add(shape);
		}
	}
	

	@Override
	public boolean test(SHShape shape) {
		return !JenaUtil.hasIndirectType(shape, SH.ConstraintComponent) && !systemShapes.contains(shape);
	}
}
