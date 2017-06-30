package org.topbraid.shacl.testcases.context;

import org.topbraid.shacl.util.SHACLUtil;

/**
 * A TestCaseContext in which SPARQL execution is preferred over JavaScript.
 * 
 * @author Holger Knublauch
 */
public class SPARQLPreferredTestCaseContext implements TestCaseContext {
	
	public static TestCaseContextFactory getTestCaseContextFactory() {
		return new TestCaseContextFactory() {
			@Override
			public TestCaseContext createContext() {
				return new SPARQLPreferredTestCaseContext();
			}
		};
	}

	
	private boolean oldValue;

	@Override
	public void setUpTestContext() {
		oldValue = SHACLUtil.isJSPreferred();
		SHACLUtil.setJSPreferred(false);
	}

	
	@Override
	public void tearDownTestContext() {
		SHACLUtil.setJSPreferred(oldValue);
	}


	@Override
	public String toString() {
		return "SPARQL preferred";
	}
}
