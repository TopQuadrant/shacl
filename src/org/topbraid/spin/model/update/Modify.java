/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.update;

import org.topbraid.spin.model.CommandWithWhere;

/**
 * A SPARQL Update operation representing a DELETE/INSERT.
 *
 * @author Holger Knublauch
 */
public interface Modify extends Update, CommandWithWhere {
}
