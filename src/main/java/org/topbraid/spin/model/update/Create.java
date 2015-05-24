package org.topbraid.spin.model.update;


/**
 * A SPARQL Update CREATE operation.
 * 
 * @author Holger Knublauch
 */
public interface Create extends Update {
	
	/**
	 * Checks if this Update operation has been marked to be SILENT.
	 * @return true if SILENT
	 */
	boolean isSilent();
}
