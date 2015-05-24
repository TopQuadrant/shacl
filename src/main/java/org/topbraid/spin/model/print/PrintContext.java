/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.print;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.util.NodeToLabelMap;


/**
 * A status object passed along during printing of SPIN expressions
 * in SPARQL syntax. 
 * 
 * @author Holger Knublauch
 */
public interface PrintContext {
	
	/**
	 * Creates a clone of this PrintContext so that it can be used recursively.
	 * @return a clone
	 */
	PrintContext clone();

	
	/**
	 * Gets the indentation level starting at 0.
	 * Indentation increases in element groups.
	 * @return the indentation level
	 * @see #setIndentation(int)
	 */
	int getIndentation();
	
	
	/**
	 * Gets an initial binding for a variable, so that a constant
	 * will be inserted into the query string instead of the variable name.
	 * @param varName  the name of the variable to match
	 * @return a literal or URI resource, or null
	 */
	RDFNode getInitialBinding(String varName);
	
	
	/**
	 * Gets the Jena NodeToLabelMap associated with this.
	 * @return the NodeToLabelMap
	 */
	NodeToLabelMap getNodeToLabelMap();
	
	
	/**
	 * Checks whether prefix declarations shall be printed into the
	 * head of the query.  By default this is switched off, but if
	 * turned on then the system should print all used prefixes.
	 * @return true  to print prefixes
	 */
	boolean getPrintPrefixes();
	
	
	/**
	 * Checks if the extra prefixes (such as afn:) shall be used to resolve
	 * qnames, even if they are not imported by the current model.
	 * @return true  if the extra prefixes shall be used
	 * @see #setUseExtraPrefixes(boolean)
	 */
	boolean getUseExtraPrefixes();
	
	
	/**
	 * Checks if resource URIs shall be abbreviated with qnames at all.  If
	 * not, then the URIs are rendered using the <...> notation.
	 * @return true  if this is using prefixes
	 */
	boolean getUsePrefixes();
	
	
	/**
	 * Checks whether any initial bindings have been declared for this context.
	 * @return true  if bindings of at least one variable exist
	 */
	boolean hasInitialBindings();
	
	
	/**
	 * Checks if we are inside of a mode (such as INSERT { GRAPH { ... } } or
	 * CONSTRUCT { ... } in which blank nodes shall be mapped to named variables
	 * such as _:b0.  This can be set temporarily using the corresponding setter
	 * but needs to be reset when done by the surrounding block. 
	 * @return true  if bnodes shall be rendered as named variables
	 */
	boolean isNamedBNodeMode();
	
	
	/**
	 * Checks if we are inside braces such as a nested expression.
	 * @return if the context is currently in nested mode
	 */
	boolean isNested();

	
	/**
	 * Prints a given string to the output stream.
	 * @param str  the String to print
	 */
	void print(String str);

	
	/**
	 * Prints the indentation string depth times.  For example,
	 * for depth=2 this might print "        ". 
	 * @param depth  the number of indentations to print
	 */
	void printIndentation(int depth);
	
	
	/**
	 * Prints a keyword to the output stream.  This can be overloaded
	 * by subclasses to do special rendering such as syntax highlighting.
	 * @param str  the keyword string
	 */
	void printKeyword(String str);
	
	
	/**
	 * Prints a line break to the output stream.  Typically this
	 * would be a /n but implementations may also do <br />.
	 */
	void println();
	
	
	/**
	 * Prints a URI to the output stream.  This can be overloaded
	 * by subclasses to do special rendering such as syntax highlighting.
	 * @param resource  the URI of the resource to print
	 */
	void printURIResource(Resource resource);
	
	
	/**
	 * Prints a variable to the output stream.  This can be overloaded
	 * by subclasses to do special rendering such as syntax highlighting.
	 * @param str  the variable string excluding the ?
	 */
	void printVariable(String str);

	
	/**
	 * Changes the indentation level.
	 * @param value  the new indentation level
	 */
	void setIndentation(int value);
	
	
	/**
	 * Activates or deactivates the mode in which bnodes are rendered as named
	 * variables, such as _:b0.
	 * @param value  true to activate, false to deactivate
	 */
	void setNamedBNodeMode(boolean value);
	
	
	/**
	 * Sets the nested flag.
	 * @param value  the new value
	 * @see #isNested()
	 */
	void setNested(boolean value);
	
	
	/**
	 * Sets the printPrefixes flag.
	 * @param value  the new value
	 * @see #getPrintPrefixes()
	 */
	void setPrintPrefixes(boolean value);
	
	
	/**
	 * Specifies whether the context shall use extra prefixes.
	 * @param value  the new value
	 * @see #getUseExtraPrefixes()
	 */
	void setUseExtraPrefixes(boolean value);
	
	
	/**
	 * Specifies whether the context shall use any prefixes at all.
	 * @param value  the new value
	 * @see #getUsePrefixes()
	 */
	void setUsePrefixes(boolean value);
}
