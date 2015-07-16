package org.topbraid.shacl.model;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * The root interface of all resources of interest to SHACL.
 * 
 * This extends Jena's Resource but it should be easy to adapt to, say, Sesame.
 * 
 * @author Holger Knublauch
 */
public interface SHACLResource extends Resource {
}
