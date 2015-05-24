/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.inference;

import java.util.Comparator;

import org.topbraid.spin.util.CommandWrapper;

/**
 * A Comparator of spin:rules to determine the order of execution.
 *
 * @author Holger Knublauch
 */
public interface SPINRuleComparator extends Comparator<CommandWrapper> {
}
