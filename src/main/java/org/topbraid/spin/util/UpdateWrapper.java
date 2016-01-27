package org.topbraid.spin.util;

import org.topbraid.spin.model.Command;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.update.Update;


/**
 * A CommandWrapper that wraps SPARQL UPDATE requests
 * (in contrast to QueryWrapper for SPARQL queries).
 * 
 * @author Holger Knublauch
 */
public class UpdateWrapper extends CommandWrapper {
	
	private Update update;
	
	private org.topbraid.spin.model.update.Update spinUpdate;
	
	
	public UpdateWrapper(Update update, Resource source, String text, org.topbraid.spin.model.update.Update spinUpdate, String label, Statement statement, boolean thisUnbound, Integer thisDepth) {
		super(source, text, label, statement, thisUnbound, thisDepth);
		this.update = update;
		this.spinUpdate = spinUpdate;
	}
	
	
	public Update getUpdate() {
		return update;
	}
	
	
	@Override
	public Command getSPINCommand() {
		return getSPINUpdate();
	}


	public org.topbraid.spin.model.update.Update getSPINUpdate() {
		return spinUpdate;
	}
}
