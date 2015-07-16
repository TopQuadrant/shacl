package org.topbraid.spin.util;

import org.apache.xerces.util.XMLChar;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.model.Command;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.print.StringPrintContext;
import org.topbraid.spin.model.update.Update;
import org.topbraid.spin.vocabulary.SP;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * A utility to convert RDF graphs between the sp:text syntax and SPIN RDF triples.
 * Can be used as a pre-processor of files so that they only use one syntax.
 * 
 * @author Holger Knublauch
 */
public class SPTextUtil {
	
	/**
	 * Adds an sp:text reflecting the SPIN RDF of a given Command.
	 * @param command  the SPIN Command to convert
	 */
	public static void addSPText(Command command) {
		MultiUnion unionGraph = new MultiUnion(new Graph[] {
				command.getModel().getGraph(),
				SPIN.getModel().getGraph()
		});
		unionGraph.setBaseGraph(command.getModel().getGraph());
		Model unionModel = ModelFactory.createModelForGraph(unionGraph);
		
		Command spinCommand = SPINFactory.asCommand(command.inModel(unionModel));
		StringPrintContext p = new StringPrintContext();
		p.setUsePrefixes(true);
		p.setPrintPrefixes(false);
		spinCommand.print(p);
		String str = p.getString();
		command.addProperty(SP.text, command.getModel().createTypedLiteral(str));
	}

	
	/**
	 * Removes any SPIN RDF syntax triples from a given Model.
	 * For example this will remove the sp:where triple tree from an sp:Select,
	 * but leave the surrounding sp:Select in place.
	 * You may want to call {@link #ensureSPTextExists(Model)} beforehand to make
	 * sure that the resulting SPIN resources remain valid.
	 * @param model  the Model to manipulate
	 */
	public static void deleteSPINRDF(Model model) {
		for(Resource type : JenaUtil.getAllSubClasses(SP.Query.inModel(SP.getModel()))) {
			for(Resource instance : model.listSubjectsWithProperty(RDF.type, type).toList()) {
				Command command = SPINFactory.asCommand(instance);
				for(Statement s : command.listProperties().toList()) {
					if(!RDF.type.equals(s.getPredicate()) && !SP.text.equals(s.getPredicate())) {
						deleteWithDependingBNodes(s);
					}
				}
			}
		}
	}
	
	
	private static void deleteWithDependingBNodes(Statement s) {
		
		// Stop if subject is a bnode with other incoming triples, e.g. a shared variable
		if(s.getSubject().isAnon()) {
			StmtIterator it = s.getModel().listStatements(null, null, s.getSubject());
			if(it.hasNext()) {  // One is expected...
				it.next();
				if(it.hasNext()) {  // ... but second is one too many
					it.close();
					return;
				}
			}
			it.close();
		}
		
		if(s.getObject().isAnon()) {
			for(Statement d : s.getResource().listProperties().toList()) {
				deleteWithDependingBNodes(d);
			}
		}
		s.remove();
	}
	
	
	/**
	 * Ensures that each SPIN Command with an sp:text also has the SPIN RDF syntax triples.
	 * For example this will create the sp:where triple for all sp:Selects, assuming they
	 * do have sp:text triples.
	 * @param model  the Model to walk through
	 */
	public static void ensureSPINRDFExists(Model model) {
		for(Resource instance : model.listSubjectsWithProperty(SP.text).toList()) {
			if(!hasSPINRDF(instance)) {
				String text = instance.getProperty(SP.text).getString();
				
				// Create SPIN RDF triples into a new temp Model
				Model baseModel = JenaUtil.createMemoryModel();
				MultiUnion unionGraph = new MultiUnion(new Graph[] {
						baseModel.getGraph(),
						model.getGraph()
				});
				unionGraph.setBaseGraph(baseModel.getGraph());
				baseModel.getGraph().getPrefixMapping().setNsPrefixes(model);
				Model tempModel = ModelFactory.createModelForGraph(unionGraph);
				Command tempCommand;
				if(SPINFactory.asCommand(instance) instanceof Update) {
					tempCommand = ARQ2SPIN.parseUpdate(text, tempModel);
				}
				else {
					tempCommand = ARQ2SPIN.parseQuery(text, tempModel);
				}
				tempCommand.removeAll(RDF.type);
				
				// Copy all remaining temp triples into old resource, redirecting some triples
				for(Statement s : baseModel.listStatements().toList()) {
					if(s.getSubject().equals(tempCommand)) {
						instance.addProperty(s.getPredicate(), s.getObject());
					}
					else {
						instance.getModel().add(s);
					}
				}
			}
		}
	}

	
	/**
	 * Ensures that each SPIN Command (query/update) in a given Model has an sp:text triple.
	 * @param model  the Model to manipulate
	 */
	public static void ensureSPTextExists(Model model) {
		for(Resource type : JenaUtil.getAllSubClasses(SP.Query.inModel(SP.getModel()))) {
			for(Resource instance : model.listSubjectsWithProperty(RDF.type, type).toList()) {
				Command command = SPINFactory.asCommand(instance);
				if(!instance.hasProperty(SP.text)) {
					addSPText(command);
				}
			}
		}
	}
	
	
	/**
	 * Checks if a given SPIN Command has at least one other triple beside the rdf:type, sp:text
	 * and spin:thisUnbound triple.  This indicates whether SPIN RDF triples exist. 
	 * @param command  the Command to check
	 * @return true if the command has SPIN RDF triples
	 */
	public static boolean hasSPINRDF(Resource command) {
		StmtIterator it = command.listProperties();
		try {
			while(it.hasNext()) {
				Statement o = it.next();
				if(!RDF.type.equals(o.getPredicate()) && !SP.text.equals(o.getPredicate()) && !SPIN.thisUnbound.equals(o.getPredicate())) {
					return true;
				}
			}
			return false;
		}
		finally {
			it.close();
		}
	}
	
	
	private static boolean isQNameCharacter(char c) {
		return c != ':' && XMLChar.isName(c);
	}
	
	
	public static String replaceQName(String text, String qname, String newQName, String newURI) {
		if(newQName == null) {
			newQName = "<" + newURI + ">";
		}
		int index = text.lastIndexOf(qname);
		while(index >= 0) {
			if((index == 0 || !isQNameCharacter(text.charAt(index - 1))) &&
					(index + qname.length() == text.length() || !isQNameCharacter(text.charAt(index + qname.length())))) {
				text = text.substring(0, index) + newQName + text.substring(index + qname.length());
			}
			index = text.lastIndexOf(qname, index - 1);
		}
		return text;
	}
	
	
	/**
	 * Checks if a given SPARQL text contains a given qname - making sure
	 * that they are true references and not part of another qname.
	 * @param text  the SPARQL text, e.g. "ASK { ?this ex:name ?name }"
	 * @param qname  the qname, e.g. "ex:name"
	 * @return true if the qname is contained in text
	 */
	public static boolean textContainsQName(String text, String qname) {
		int index = text.indexOf(qname);
		while(index >= 0) {
			if((index == 0 || !isQNameCharacter(text.charAt(index - 1))) &&
					(index + qname.length() == text.length() || !isQNameCharacter(text.charAt(index + qname.length())))) {
				return true;
			}
			index = text.indexOf(qname, index + 1);
		}
		return false;
	}
}
