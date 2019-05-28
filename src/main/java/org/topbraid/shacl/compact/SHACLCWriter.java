package org.topbraid.shacl.compact;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.writer.WriterGraphRIOTBase;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.arq.SHACLPaths;
import org.topbraid.shacl.vocabulary.SH;

/**
 * This is an incomplete converter from RDF graphs to SHACLC format that is barely tested.
 * SHACLC only covers a subset of RDF and SHACL, so not all SHACL graphs can be meaningfully represented.
 * 
 * @author Holger Knublauch
 */
public class SHACLCWriter extends WriterGraphRIOTBase {
	
	private static Set<Property> specialPropertyProperties = Sets.newHashSet(
			RDF.type, SH.path, SH.datatype, SH.class_, SH.minCount, SH.maxCount, SH.node, SH.nodeKind
	);
	
	private static Set<Property> specialShapeProperties = Sets.newHashSet(
			RDF.type, SH.property, SH.node
	);

	@Override
	public Lang getLang() {
		return SHACLC.lang;
	}
	
	
	protected void warn(String message) {
		System.err.println("Warning: " + message);
	}

	
	@Override
	public void write(Writer out, Graph graph, PrefixMap prefixMap, String baseURI, Context context) {
        IndentedWriter iOut = RiotLib.create(out);
        iOut.setUnitIndent(1);
        iOut.setPadChar('\t');
		write(iOut, graph, prefixMap, baseURI, context);
	}

	
	@Override
	public void write(OutputStream out, Graph graph, PrefixMap prefixMap, String baseURI, Context context) {
        IndentedWriter iOut = new IndentedWriter(out);
        iOut.setUnitIndent(1);
        iOut.setPadChar('\t');
		write(iOut, graph, prefixMap, baseURI, context);
	}
	
	
	private void write(IndentedWriter out, Graph graph, PrefixMap prefixMap, String baseURI, Context context) {
		Model model = ModelFactory.createModelForGraph(graph);
		out.println("BASE <" + baseURI + ">");
		out.println();
		writeImports(out, model.getResource(baseURI));
		writePrefixes(out, prefixMap);
		writeShapes(out, model);
		out.flush();
	}
	
	
	private void writeImports(IndentedWriter out, Resource ontology) {
		List<Resource> imports = JenaUtil.getResourceProperties(ontology, OWL.imports);
		Collections.sort(imports, new Comparator<Resource>() {
			@Override
			public int compare(Resource o1, Resource o2) {
				return o1.getURI().compareTo(o2.getURI());
			}
			
		});
		if(!imports.isEmpty()) {
			for(Resource imp : imports) {
				out.println("IMPORTS <" + imp.getURI() + ">");
			}
			out.println();
		}
	}
	
	
	private void writePrefixes(IndentedWriter out, PrefixMap prefixMap) {
		List<String> prefixes = new LinkedList<String>(prefixMap.getMapping().keySet());
		if(!prefixes.isEmpty()) {
			Collections.sort(prefixes);
			for(String prefix : prefixes) {
				if(SHACLC.getDefaultPrefixURI(prefix) == null) {
					out.println("PREFIX " + prefix + ": <" + prefixMap.expand(prefix + ":") + ">");
				}
			}
			out.println();
		}
	}
	
	
	private void writeShapes(IndentedWriter out, Model model) {
		
		List<Resource> shapes = new LinkedList<>();
		for(Resource shape : JenaUtil.getAllInstances(SH.NodeShape.inModel(model))) {
			if(shape.isURIResource()) {
				shapes.add(shape);
			}
		}
		// Collections.sort(shapes, ResourceComparator.get());
		
		for(int i = 0; i < shapes.size(); i++) {
			if(i > 0) {
				out.println();
			}
			writeShape(out, shapes.get(i));
		}
	}
	
	
	private void writeShape(IndentedWriter out, Resource shape) {
		out.print("" + iri(shape));
		writeShapeBody(out, shape);
	}
	
	
	private void writeShapeBody(IndentedWriter out, Resource shape) {
		writeExtraStatements(out, shape, specialShapeProperties, false);
		out.println(" {");
		out.incIndent();
		List<Resource> properties = new LinkedList<>();
		for(Resource property : JenaUtil.getResourceProperties(shape, SH.property)) {
			properties.add(property);
		}
		Collections.sort(properties, new Comparator<Resource>() {
			@Override
			public int compare(Resource arg1, Resource arg2) {
				String path1 = getPathString(arg1);
				String path2 = getPathString(arg2);
				return path1.compareTo(path2);
			}
		});
		for(Resource property : properties) {
			writeProperty(out, property);
		}
		
		out.decIndent();
		out.println("}");
	}

	
	private void writeProperty(IndentedWriter out, Resource property) {
		out.print(getPathString(property));
		out.print(" ");
		
		out.print(getPropertyTypes(property));
		
		// Count block
		out.print(" ");
		out.print("[");
		Statement minCountS = property.getProperty(SH.minCount);
		if(minCountS != null) {
			out.print("" + minCountS.getInt());
		}
		else {
			out.print("0");
		}
		out.print("..");
		Statement maxCountS = property.getProperty(SH.maxCount);
		if(maxCountS != null) {
			out.print("" + maxCountS.getInt());
		}
		else {
			out.print("*");
		}
		out.print("]");
		
		writeExtraStatements(out, property, specialPropertyProperties, false);
		
		writeNestedShapes(out, property);
		
		out.println(" ;");
	}


	private void writeExtraStatements(IndentedWriter out, Resource subject, Set<Property> specialProperties, boolean wrapped) {
		List<Statement> extras = getExtraStatements(subject, specialProperties);
		if(!extras.isEmpty()) {
			if(wrapped) {
				out.print("( ");
			}
			for(Statement s : extras) {
				out.print(" " + getPredicateName(s.getPredicate()));
				out.print("=");
				out.print(node(s.getObject()));
			}
			if(wrapped) {
				out.print(" )");
			}
		}
	}
	
	
	private void writeNestedShapes(IndentedWriter out, Resource subject) {
		for(Resource node : JenaUtil.getResourceProperties(subject, SH.node)) {
			if(node.isAnon()) {
				writeShapeBody(out, node);
			}
		}
	}
	
	
	private List<Statement> getExtraStatements(Resource subject, Set<Property> specialProperties) {
		List<Statement> results = new LinkedList<>();
		for(Statement s : subject.listProperties().toList()) {
			if(SH.NS.equals(s.getPredicate().getNameSpace()) && !specialProperties.contains(s.getPredicate()) && !s.getObject().isAnon()) {
				results.add(s);
			}
		}
		Collections.sort(results, new Comparator<Statement>() {
			@Override
			public int compare(Statement o1, Statement o2) {
				String pred1 = getPredicateName(o1.getPredicate());
				String pred2 = getPredicateName(o2.getPredicate());
				int preds = pred1.compareTo(pred2);
				if(preds != 0) {
					return preds;
				}
				else {
					String lex1 = node(o1.getObject());
					String lex2 = node(o2.getObject());
					return lex1.compareTo(lex2);
				}
			}
		});
		return results;
	}
	
	
	private String getPathString(Resource property) {
		Resource path = JenaUtil.getResourceProperty(property, SH.path);
		return SHACLPaths.getPathString(path);
	}
	
	
	private String getPredicateName(Property predicate) {
		return predicate.getLocalName();
	}
	
	
	private String getPropertyTypes(Resource property) {
		List<String> types = new LinkedList<>();
		for(Resource clas : JenaUtil.getResourceProperties(property, SH.class_)) {
			types.add(iri(clas));
		}
		for(Resource datatype : JenaUtil.getResourceProperties(property, SH.datatype)) {
			types.add(iri(datatype));
		}
		for(Resource node : JenaUtil.getResourceProperties(property, SH.node)) {
			if(node.isURIResource()) {
				types.add("@" + iri(node));
			}
		}
		Resource nodeKind = JenaUtil.getResourceProperty(property, SH.nodeKind);
		if(nodeKind != null) {
			types.add(nodeKind.getLocalName());
		}
		Collections.sort(types);
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < types.size(); i++) {
			if(i > 0) {
				sb.append(" ");
			}
			sb.append(types.get(i));
		}
		return sb.toString();
	}
	
	
	private String iri(Resource resource) {
		String qname = resource.getModel().qnameFor(resource.getURI());
		if(qname != null) {
			return qname;
		}
		else {
			return "<" + resource.getURI() + ">";
		}
	}
	
	
	private String node(RDFNode node) {
		if(node.isURIResource()) {
			return iri((Resource)node);
		}
		else if(node.isLiteral()) {
			return FmtUtils.stringForNode(node.asNode());
		}
		else {
			// TODO?
			return null;
		}
	}
}
