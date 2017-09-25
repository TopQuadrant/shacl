/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

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
