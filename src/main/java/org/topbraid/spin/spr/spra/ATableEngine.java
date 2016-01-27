package org.topbraid.spin.spr.spra;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.topbraid.spin.spr.AbstractTableEngine;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.vocabulary.SPRA;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;


/**
 * Implementation of the SPR protocol http://spinrdf.org/spra
 * 
 * @author Holger Knublauch
 */
public class ATableEngine extends AbstractTableEngine {

	@Override
	public Resource createTable(Model model, ResultSet rs) {
		String ns = SPRA.NS;
		
		String id = AnonId.create().getLabelString().replaceAll(":", "_");
		Resource table = model.getResource("http://atables.org/data" + id);
		
		List<String> varNames = rs.getResultVars();
		addVarNames(ns, table, varNames);

		// First copy the cells into an auxiliary data structure
		// (avoiding concurrency changes under the hood that might impact the ResultSet)
		Property valueProperty = getValueProperty(ns);
		Map<Resource,RDFNode> cell2Value = new HashMap<Resource,RDFNode>();
		int row = 0;
		for( ; rs.hasNext(); row++) {
			QuerySolution qs = rs.next();
			for(int col = 0; col < varNames.size(); col++) {
				String varName = varNames.get(col);
				RDFNode value = qs.get(varName);
				if(value != null) {
					Resource cell = getCell(table, row, col);
					cell2Value.put(cell, value);
				}
			}
		}

		// Now create the actual triples
		table.addProperty(RDF.type, SPRA.Table);
		for(Resource cell : cell2Value.keySet()) {
			RDFNode value = cell2Value.get(cell);
			cell.addProperty(valueProperty, value);
		}
		
		table.addProperty(getColCountProperty(ns), JenaDatatypes.createInteger(varNames.size()));
		table.addProperty(getRowCountProperty(ns), JenaDatatypes.createInteger(row));
		
		return table;
	}
	
	
	private Resource getCell(Resource table, int row, int col) {
		return table.getModel().getResource(table.getURI() + "-r" + row + "-c" + col);
	}
}
