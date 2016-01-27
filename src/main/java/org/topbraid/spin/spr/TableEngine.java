package org.topbraid.spin.spr;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;


/**
 * An interface for objects that can convert a SPARQL ResultSet
 * into a SPR table.
 * 
 * @author Holger Knublauch
 */
public interface TableEngine {

	Resource createTable(Model model, ResultSet rs);
}
