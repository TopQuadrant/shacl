package org.topbraid.shacl.testcases.context;

import org.topbraid.shacl.util.SHACLUtil;

/**
 * A TestCaseContext in which JavaScript execution is preferred over SPARQL.
 * 
 * @author Holger Knublauch
 */
public class JSPreferredTestCaseContext implements TestCaseContext {
	
	public static TestCaseContextFactory getTestCaseContextFactory() {
		return new TestCaseContextFactory() {
			@Override
			public TestCaseContext createContext() {
				return new JSPreferredTestCaseContext();
			}
		};
	}

	
	private boolean oldValue;

	@Override
	public void setUpTestContext() {
		oldValue = SHACLUtil.isJSPreferred();
		SHACLUtil.setJSPreferred(true);
	}

	
	@Override
	public void tearDownTestContext() {
		SHACLUtil.setJSPreferred(oldValue);
	}


	@Override
	public String toString() {
		return "JavaScript preferred";
	}
}
