package org.topbraid.shacl.model;

import org.apache.jena.rdf.model.Resource;

public interface SHConstraintComponent extends SHParameterizable {

	Resource getValidator(Resource executableType, Resource context);
}
