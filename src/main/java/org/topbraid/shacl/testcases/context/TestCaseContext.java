package org.topbraid.shacl.testcases.context;

/**
 * An interface for objects that can run certain tests within a given context.
 * A context consists of a set-up step and a tear-down step.
 * It can, for example, be used to switch execution from preferring SPARQL
 * to preferring JavaScript.
 * 
 * @author Holger Knublauch
 */
public interface TestCaseContext {

	void setUpTestContext();
	
	void tearDownTestContext();
}
