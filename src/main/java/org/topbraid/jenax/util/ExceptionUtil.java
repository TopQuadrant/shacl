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

import java.io.PrintStream;
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
	 * @return Never returns, return type is for idiom "throw throwUnchecked();" to clarify that next line is not reached.
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
	 * @return Never returns, return type is for idiom "throw throwUnchecked();" to clarify that next line is not reached.
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
				rslt = clazz.getDeclaredConstructor().newInstance();
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

	/**
	 * Returns the given throwable's message, or the message
	 * from its cause if the given throwable has no own message.
	 * Will return null if no explicit message is provided
	 * in the throwable or any of its causes.
	 * 
	 * The intent is to get "The real message" instead of
	 * "x.y.SomeException: The real message" when the throwable
	 * bearing the real message is wrapped without a new message.
	 * 
	 * @see Throwable#getMessage()
	 * @see Throwable#getCause()
	 */
	public static String getDeepMessage(Throwable t) {
		String msg = t.getMessage();
		if (t.getCause() == null) {
			// No further wrapped throwable -- this message is the best we got
			return msg;
		}
		if (msg == null) {
			// No message here, try to get one from wrapped throwable
			return getDeepMessage(t.getCause());
		}
		if (msg.equals(t.getCause().toString())) {
			// It's the default message generated by the "new Throwable(throwable)" constructor.
			// Skip it, and get message from the wrapped throwable instead
			return getDeepMessage(t.getCause());
		}
		// It's a custom message, good!
		return msg;
	}
	
	public static String getStackTrace(Throwable t) {
		if (t == null) return null;
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

	/**
	 * True if the given throwable or one of its causes (via {@link Throwable#getCause()})
	 * is an instance of the given class. 
	 */
	public static boolean hasDeepCause(Throwable t, Class<? extends Throwable> throwableClass) {
		return t != null && getDeepCause(t, throwableClass) != null;
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

	/**
	 * Returns a shortened form of the given stack trace
	 * by removing "boring" lines. The first "interesting" method
	 * is given as an argument. All lines below in the stack trace,
	 * that is, the caller of the "interesting" method, and its caller,
	 * and so on, up to the origination point of the thread, are omitted.
	 *
	 * @param stackTrace The input stack trace, as produced by {@link #getStackTrace(Throwable)}
	 * @param shallowestInterestingMethod A fully qualified method name, as it appears in a stack trace: org.example.MyClass.myMethod
	 * @return A shortened stack trace that omits the callers of the class
	 */
	public static String shortenStackTrace(String stackTrace, String shallowestInterestingMethod) {
		int idx = stackTrace.indexOf("\tat " + shallowestInterestingMethod + "(");
		if (idx < 0) {
			// Execution did not pass through our interesting boundary class.
			// Return whole stack trace.
			return stackTrace;
		}
		int boringStart = stackTrace.indexOf(System.lineSeparator(), idx);
		if (boringStart < 0) {
			// The interesting boundary class seems to be the
			// last line of the stack trace. Return whole stack trace.
			return stackTrace;
		}
		boringStart += System.lineSeparator().length();
		StringBuilder result = new StringBuilder(stackTrace.substring(0, boringStart));
		result.append("\t... Rest omitted");
		result.append(System.lineSeparator());
		int causeStart = stackTrace.indexOf("Caused by:", boringStart);
		int suppressedStart = stackTrace.indexOf("Suppressed:", boringStart);
		if (causeStart >= 0 || suppressedStart >= 0) {
			result.append(stackTrace.substring(Math.max(causeStart, suppressedStart)));
		}
		return result.toString();
	}

	/**
	 * Returns a shortened form of the given stack trace,
	 * omitting lines related to the servlet container before entry
	 * into the servlet implementation.
	 *
	 * @see #shortenStackTrace(String, String)
	 */
	public static String shortenStackTraceForServletCall(String stackTrace) {
		return shortenStackTrace(stackTrace, "javax.servlet.http.HttpServlet.service");
	}

	/**
	 * Returns a version of the given throwable that prints with
	 * a shortened stack trace, omitting lines related to the servlet
	 * container before entry into the servlet implementation.
	 *
	 * The resulting exception is only intended for printing/logging
	 * its stack trace. It is not intended to be thrown or wrapped into
	 * other exceptions.
	 *
	 * @see #shortenStackTrace(String, String)
	 */
	@SuppressWarnings("serial")
	public static Throwable withServletContainerStackOmitted(Throwable t) {
		if (t == null) return null;
		return new Throwable(t) {
			@Override
			public synchronized void printStackTrace(PrintStream out) {
				out.print(getShortenedStackTrace());
			}
			@Override
			public synchronized void printStackTrace(PrintWriter out) {
				out.print(getShortenedStackTrace());
			}
			@Override
			public synchronized Throwable fillInStackTrace() {
				// This doesn't need to have its own stack trace - better performance
				return this;
			}
			private String getFullStackTrace() {
				StringWriter writer = new StringWriter();
				getCause().printStackTrace(new PrintWriter(writer));
				return writer.getBuffer().toString();
			}
			private String getShortenedStackTrace() {
				return shortenStackTraceForServletCall(getFullStackTrace());
			}
		};
	}
}
