package org.topbraid.spin.model.update;


/**
 * A SPARQL Update DROP operation.
 * 
 * @author Holger Knublauch
 */
public interface Drop extends Update {
	
	/**
	 * Checks if this Update operation has been marked to be SILENT.
	 * @return true if SILENT
	 */
	boolean isSilent();
}
