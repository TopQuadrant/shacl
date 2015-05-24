/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved.
 *******************************************************************************/
package org.topbraid.spin.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * A simple implementation of the ParameterProvider interface, based
 * on a HashMap.
 * 
 * @author Holger Knublauch
 */
public class SimpleParameterProvider implements ParameterProvider {

	private final Map<String,String> map;


	public SimpleParameterProvider() {
		this(new HashMap<String,String>());
	}


	public SimpleParameterProvider(Map<String,String> map) {
		this.map = map;
	}
	
	
	/**
	 * Adds a new entry to the internal Map.
	 * This is typically used in conjunction with the constructor
	 * without arguments.
	 * @param key  the parameter key
	 * @param value  the value
	 */
	public void add(String key, String value) {
		map.put(key, value);
	}


	public String getParameter(String key) {
		return map.get(key);
	}


	public Iterator<String> listParameterNames() {
		return map.keySet().iterator();
	}
}
