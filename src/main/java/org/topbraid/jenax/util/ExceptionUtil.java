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
package org.topbraid.jenax.util;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * A collection of utilities on Exception handling.
 */
public class ExceptionUtil {

	/**
	 * Does not return:
	 * throws an unchecked exception, based on <code>t</code>.
	 * First, the <code>getCause</code> chain of <code>t</code>
	 * is followed to its base, and then,
	 * an appropriate throwable exception is thrown, with preference
	 * to throwing an Error.
	 *
	 * @param t The underlying problem.
	 * @throws Error If there is an underlying Error
	 * @throws RuntimeException Otherwise
	 */
	public static RuntimeException throwRootCauseUnchecked(Throwable t) {
		return ExceptionUtil.throwDeepCauseChecked(t,RuntimeException.class);
	}

	
	/**
	 * Does not return:
	 * throws an unchecked exception, based on <code>t</code>.
	 * If <code>t</code> can be thrown directly, it is,
	 * otherwise it is wrapped as a <code>RuntimeException</code>.
	 *
	 * @param t The underlying problem.
	 * @throws Error If t is an Error
	 * @throws RuntimeException Otherwise
	 * @return Never returns, return type is for idiom "throw throwUnchecked();" to clarify that next line is not reached.
	 */
	public static RuntimeException throwUnchecked(Throwable t) {
		if (t == null ) {
			throw new NullPointerException();
		}
		if (t instanceof RuntimeException) {
			throw (RuntimeException)t;
		}
		if (t instanceof Error) {
			throw (Error)t;
		}
		throw new RuntimeException(t);
	}

	
	/**
	 * Always 
	 * throw an exception; based on <code>t</code>.
	 * The <code>getCause</code> chain of <code>t</code>
	 * is analyzed, and then,
	 * an appropriate Throwable is thrown, with preference as follows:
	 * <ol>
	 * <li>An Error.</li>
	 * <li>An exception that is of type <code>clazz</code> (or a subtype).</li>
	 * <li>A runtime exception.</li>
	 * <li>A new Exception of type clazz that has cause <code>t</code></li>
	 * </ol>
	 * If one of the first three is thrown, then it is the first one of that type in the causal chain.
	 *
	 * @param t The underlying problem.
	 * @param clazz The class of exception to look for. This clazz must not be abstract and must have
	 * either a no element constructor or a constructor with one argument being a throwable.
	 * @param  <EX> The type of exception thrown by the constructor of V
	 * @throws Error If there is an underlying Error
	 * @throws RuntimeException If there is an underlying RuntimeException and no exception of type EX
	 * @throws EX If there is an appropriate exception or otherwise
	 * @throws IllegalArgumentException If clazz is inappropriate (not always checked).
	 */
	public static <EX extends Throwable> EX throwDeepCauseChecked(Throwable t, Class<? extends EX> clazz) throws EX {
		if (t == null ) {
		   throw new NullPointerException();
	    }
	    Error firstError=null;
	    EX firstEX=null;
	    RuntimeException firstRTE=null;
	    // Walk chain finding first item of each interesting class.
	    for (Throwable tt=t;tt!=null;tt=tt.getCause()) {
	    	firstError = ExceptionUtil.chooseNonNullCorrectClass(Error.class,firstError,tt);
	    	firstEX = ExceptionUtil.chooseNonNullCorrectClass(clazz,firstEX,tt);
	    	firstRTE = ExceptionUtil.chooseNonNullCorrectClass(RuntimeException.class,firstRTE,tt);
	    }
	    if (firstError!=null) { throw firstError; }
	    if (firstEX!=null) { throw firstEX; }
	    if (firstRTE!=null) { throw firstRTE; }
	    
	    // Wrap original problem in clazz.
	    EX rslt = null;
	    try {
	    	rslt = clazz.getConstructor(Throwable.class).newInstance(t);
		}
		catch (Exception e) {
			try {
				rslt = clazz.newInstance();
				rslt.initCause(t);
			}
			catch (Exception e1) {
				if (e1.getCause()==null) {
					e1.initCause(t);
				}
				throw new IllegalArgumentException(
						clazz.getName()+" does not have a functioning constructor, with either no arguments or a Throwable argument.",
						e1);
			}
		}
		throw rslt;
		// NOT REACHED.
	}

	public static String getStackTrace(Throwable t) {
		StringWriter writer = new StringWriter();
		t.printStackTrace(new PrintWriter(writer));
		return writer.getBuffer().toString();
	}

	@SuppressWarnings({ "unchecked" })
	private static <TT extends Throwable> TT chooseNonNullCorrectClass(Class<? extends TT> clazz, TT firstError, Throwable tt) {
		if (firstError != null) { return firstError; }
		if (clazz.isInstance(tt)) { return (TT)tt; }
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <EX extends Throwable> EX getDeepCause(Throwable t, Class<? extends EX> clazz) {
		if (t == null ) {
		   throw new NullPointerException();
	    }
	    // Walk chain finding first item of each interesting class.
	    for (Throwable tt=t;tt!=null;tt=tt.getCause()) {
			if (clazz.isInstance(tt)) { 
				return (EX)tt; 
			}
	    }
		return null;
	}
}
