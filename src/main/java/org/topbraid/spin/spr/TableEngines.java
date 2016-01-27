package org.topbraid.spin.spr;

import java.util.HashMap;
import java.util.Map;

import org.topbraid.spin.spr.spra.ATableEngine;
import org.topbraid.spin.vocabulary.SPRA;

import org.apache.jena.rdf.model.Resource;


/**
 * A Singleton managing the registered TableEngines.
 * 
 * @author Holger Knublauch
 */
public class TableEngines {

	private static TableEngines singleton = new TableEngines();
	
	public static TableEngines get() {
		return singleton;
	}
	
	public static void set(TableEngines value) {
		TableEngines.singleton = value;
	}
	
	private TableEngine defaultTableEngine = new ATableEngine();
	
	private Map<Resource,TableEngine> map = new HashMap<Resource,TableEngine>();
	
	public TableEngines() {
		map.put(SPRA.Table, defaultTableEngine);
	}
	
	
	public TableEngine getDefaultTableEngine() {
		return defaultTableEngine;
	}
	
	
	public TableEngine getForType(Resource type) {
		return map.get(type);
	}
	
	
	public void register(Resource type, TableEngine tableEngine) {
		map.put(type, tableEngine);
	}
}
