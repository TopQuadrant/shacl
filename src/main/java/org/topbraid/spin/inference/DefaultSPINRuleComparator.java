/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.inference;

import java.util.ArrayList;
import java.util.List;

import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.CommandWrapper;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

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
