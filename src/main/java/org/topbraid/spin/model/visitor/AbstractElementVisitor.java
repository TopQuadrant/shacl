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

package org.topbraid.spin.model.visitor;

import org.topbraid.spin.model.Bind;
import org.topbraid.spin.model.ElementList;
import org.topbraid.spin.model.Exists;
import org.topbraid.spin.model.Filter;
import org.topbraid.spin.model.Minus;
import org.topbraid.spin.model.NamedGraph;
import org.topbraid.spin.model.NotExists;
import org.topbraid.spin.model.Optional;
import org.topbraid.spin.model.Service;
import org.topbraid.spin.model.SubQuery;
import org.topbraid.spin.model.TriplePath;
import org.topbraid.spin.model.TriplePattern;
import org.topbraid.spin.model.Union;
import org.topbraid.spin.model.Values;


/**
 * Basic, "empty" implementation of ElementVisitor.
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractElementVisitor implements ElementVisitor {


	@Override
    public void visit(Bind let) {
	}

	
	@Override
    public void visit(ElementList elementList) {
	}

	
	@Override
    public void visit(Exists exists) {
	}


	@Override
    public void visit(Filter filter) {
	}


	@Override
	public void visit(Minus minus) {
	}


	@Override
    public void visit(NamedGraph namedGraph) {
	}
	
	
	@Override
    public void visit(NotExists notExists) {
	}


	@Override
    public void visit(Optional optional) {
	}


	@Override
    public void visit(Service service) {
	}


	@Override
    public void visit(SubQuery subQuery) {
	}


	@Override
    public void visit(TriplePath triplePath) {
	}


	@Override
    public void visit(TriplePattern triplePattern) {
	}


	@Override
    public void visit(Union union) {
	}


	@Override
	public void visit(Values values) {
	}
}
