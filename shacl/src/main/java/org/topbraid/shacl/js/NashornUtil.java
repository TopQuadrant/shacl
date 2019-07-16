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
package org.topbraid.shacl.js;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Utility methods to work with Nashorn objects using reflection - in cases where
 * accessing the Nashorn JDK is not possible due to classloader restrictions.
 * 
 * @author Holger Knublauch
 */
public class NashornUtil {
	
	
	public static Object[] asArray(Object obj) throws Exception {
	   final Method values = obj.getClass().getMethod("values");
       final Object vals = values.invoke(obj);
       if (vals instanceof Collection<?>) {
           final Collection<?> coll = (Collection<?>) vals;
           return coll.toArray(new Object[0]);
       }
       else {
    	   return null;
       }
	}
	

	public static boolean isArray(Object obj) throws Exception {
		if(obj != null && obj.getClass().getName().equals("jdk.nashorn.api.scripting.ScriptObjectMirror")) {
			final Method isArray = obj.getClass().getMethod("isArray");
			final Object result = isArray.invoke(obj);
            return result != null && result.equals(true);
		}
		else {
			return false;
		}
	}
}
