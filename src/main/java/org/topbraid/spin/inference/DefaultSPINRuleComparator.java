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

package org.topbraid.spin.inference;

import java.util.ArrayList;
import java.util.List;

import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.CommandWrapper;
import org.topbraid.spin.vocabulary.SPIN;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * A SPINRuleComparator using the spin:nextRuleProperty property.
 *
 * @author Holger Knublauch
 */
public class DefaultSPINRuleComparator implements SPINRuleComparator {
	
	private List<Resource> properties;
	
	
	public DefaultSPINRuleComparator(Model model) {
		// Pre-build properties list
		Property spinRule = model.getProperty(SPIN.rule.getURI());
		properties = new ArrayList<Resource>(JenaUtil.getAllSubProperties(spinRule));
		properties.add(spinRule);
		
		// Compare all and change order accordingly
		boolean again = true;
		while(again) {
			again = false;
			for(int j = 0; j < properties.size(); j++) {
				for(int i = j + 1; i < properties.size(); i++) {
					Resource oi = properties.get(i);
					Resource oj = properties.get(j);
					if(oi.hasProperty(SPIN.nextRuleProperty, oj)) {
						properties.set(i, oj);
						properties.set(j, oi);
						again = true;
					}
				}
			}
		}
	}
	

	@Override
    public int compare(CommandWrapper w1, CommandWrapper w2) {
		if(properties.size() > 1) {
			Property p1 = w1.getStatement() != null ? w1.getStatement().getPredicate() : SPIN.rule;
			Property p2 = w2.getStatement() != null ? w2.getStatement().getPredicate() : SPIN.rule;
			if(!p1.equals(p2)) {
				int index1 = properties.indexOf(p1);
				int index2 = properties.indexOf(p2);
				int compare = Integer.valueOf(index1).compareTo(index2);
				if(compare != 0) {
					return compare;
				}
			}
		}
		
		return w1.getText().compareTo(w2.getText());
	}
}
