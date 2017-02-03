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
