/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved.
 *******************************************************************************/
package org.topbraid.spin.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * A set that uses ConcurrentHashMap as its implementation.
 * 
 * @author Jeremy
 *
 * @param <E>
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> {

	private final ConcurrentMap<E,Boolean> delegate = new ConcurrentHashMap<E,Boolean>();

	public ConcurrentHashSet() {
	}

	@Override
	public boolean add(E o) {
		if (o == null) {
			return false;
		}
		return delegate.putIfAbsent(o, Boolean.TRUE) == null;
	}


	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public boolean contains(Object o) {
		return delegate.containsKey(o);
	}


	@Override
	public Iterator<E> iterator() {
		return delegate.keySet().iterator();
	}


	@Override
	public boolean remove(Object o) {
		if (o == null) {
			return false;
		}
		return delegate.remove(o) != null;
	}


	@Override
	public int size() {
		return delegate.keySet().size();
	}

}
