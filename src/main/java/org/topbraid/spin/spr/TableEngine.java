package org.topbraid.spin.spr;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * An interface for objects that can convert a SPARQL ResultSet
 * into a SPR table.
 * 
 * @author Holger Knublauch
 */
public interface TableEngine {

	Resource createTable(Model model, ResultSet rs);
}
