package org.topbraid.shacl.compact;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.ReaderRIOTBase;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.jenax.util.ExceptionUtil;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.compact.parser.SHACLCBaseListener;
import org.topbraid.shacl.compact.parser.SHACLCLexer;
import org.topbraid.shacl.compact.parser.SHACLCListener;
import org.topbraid.shacl.compact.parser.SHACLCParser;
import org.topbraid.shacl.compact.parser.SHACLCParser.BaseDeclContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.ConstraintContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.ImportsDeclContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.IriContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.IriOrLiteralContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.IriOrLiteralOrArrayContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.NodeNotContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.NodeOrContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.NodeShapeBodyContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.NodeShapeContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.NodeValueContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.PathAlternativeContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.PathContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.PathEltContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.PathEltOrInverseContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.PathPrimaryContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.PathSequenceContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.PrefixDeclContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.PropertyAtomContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.PropertyCountContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.PropertyNotContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.PropertyOrContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.PropertyShapeContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.ShaclDocContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.ShapeClassContext;
import org.topbraid.shacl.compact.parser.SHACLCParser.TargetClassContext;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

public class SHACLCReader extends ReaderRIOTBase {
	
	private PrefixMapping prefixMapping = new PrefixMappingImpl();
	
	
    public SHACLCReader() {
        JenaUtil.initNamespaces(prefixMapping);
    }

	
    @Override
	public void read(InputStream in, String baseURI, Lang lang, StreamRDF output, Context context) {
		try {
			SHACLCLexer lexer = new SHACLCLexer(new ANTLRInputStream(in));
			read(lexer, baseURI, output);
		}
		catch(IOException ex) {
			ExceptionUtil.throwUnchecked(ex);
		}
	}

	
	@Override
	public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
		try {
			SHACLCLexer lexer = new SHACLCLexer(new ANTLRInputStream(reader));
			read(lexer, baseURI, output);
		}
		catch(IOException ex) {
			ExceptionUtil.throwUnchecked(ex);
		}
	}


	private void read(SHACLCLexer lexer, String baseURI, StreamRDF output) {
		
		for(String prefix : SHACLC.getDefaultPrefixes()) {
			output.prefix(prefix, SHACLC.getDefaultPrefixURI(prefix));
		}
		
		final Node[] ontology = new Node[] { NodeFactory.createURI(baseURI) };
		Set<Node> imports = new HashSet<>();
		
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SHACLCParser parser = new SHACLCParser(tokens);
		ShaclDocContext docContext = parser.shaclDoc();
		ParseTreeWalker walker = new ParseTreeWalker();
		SHACLCListener listener = new SHACLCBaseListener() {

			@Override
			public void exitBaseDecl(BaseDeclContext ctx) {
				String text = ctx.IRIREF().getText();
				String uri = text.substring(1, text.length() - 1);
				ontology[0] = NodeFactory.createURI(uri);
			}

			@Override
			public void exitImportsDecl(ImportsDeclContext ctx) {
				String text = ctx.IRIREF().getText();
				String uri = text.substring(1, text.length() - 1);
				imports.add(NodeFactory.createURI(uri));
			}

			@Override
			public void exitPrefixDecl(PrefixDeclContext ctx) {
				String prefixText = ctx.PNAME_NS().getText();
				String prefix = prefixText.substring(0, prefixText.length() - 1).trim();
				String nsText = ctx.IRIREF().getText();
				String ns = nsText.substring(1, nsText.length() - 1);
				output.prefix(prefix, ns);
				prefixMapping.setNsPrefix(prefix, ns);
			}

			@Override
			public void exitNodeShape(NodeShapeContext ctx) {
				Node nodeShape = getURI(ctx.iri().getText());
				output.triple(Triple.create(nodeShape, RDF.type.asNode(), SH.NodeShape.asNode()));
				TargetClassContext targetClass = ctx.targetClass();
				if(targetClass != null) {
					for(IriContext c : targetClass.iri()) {
						Node object = getURI(c.getText());
						output.triple(Triple.create(nodeShape, SH.targetClass.asNode(), object));
					}
				}
				parseConstraints(ctx.nodeShapeBody(), nodeShape);
			}
			
			
			private Node getNode(IriOrLiteralOrArrayContext c) {
				if(c.array() != null) {
					List<Node> members = new LinkedList<>();
					for(IriOrLiteralContext i : c.array().iriOrLiteral()) {
						members.add(getNode(i));
					}
					return getNodeList(members.iterator());
				}
				else {
					return getNode(c.iriOrLiteral());
				}
			}
			
			
			private Node getNodeList(Iterator<Node> it) {
				if(!it.hasNext()) {
					return RDF.nil.asNode();
				}
				else {
					Node item = NodeFactory.createBlankNode();
					output.triple(Triple.create(item, RDF.first.asNode(), it.next()));
					output.triple(Triple.create(item, RDF.rest.asNode(), getNodeList(it)));
					return item;
				}
			}

			
			private Node getNode(IriOrLiteralContext c) {
				if(c.iri() != null) {
					return getURI(c.getText());
				}
				else {
					return NodeFactoryExtra.parseNode(c.literal().getText());
				}
			}
			
			
			private Node getPath(PathContext c) {
				return getPathAlternative(c.pathAlternative());
			}
			
			
			private Node getPathAlternative(PathAlternativeContext c) {
				if(c.pathSequence().size() > 1) {
					List<Node> members = new LinkedList<>();
					for(PathSequenceContext s : c.pathSequence()) {
						members.add(getPathSequence(s));
					}
					Node alt = NodeFactory.createBlankNode();
					output.triple(Triple.create(alt, SH.alternativePath.asNode(), getNodeList(members.iterator())));
					return alt;
				}
				else {
					return getPathSequence(c.pathSequence(0));
				}
			}
			
			private Node getPathSequence(PathSequenceContext c) {
				if(c.pathEltOrInverse().size() > 1) {
					List<Node> members = new LinkedList<>();
					for(PathEltOrInverseContext s : c.pathEltOrInverse()) {
						members.add(getPathEltOrInverse(s));
					}
					return getNodeList(members.iterator());
				}
				else {
					return getPathEltOrInverse(c.pathEltOrInverse(0));
				}
			}
			
			private Node getPathEltOrInverse(PathEltOrInverseContext c) {
				if(c.pathInverse() != null) {
					Node inversePath = NodeFactory.createBlankNode();
					output.triple(Triple.create(inversePath, SH.inversePath.asNode(), getPathElt(c.pathElt())));
					return inversePath;
				}
				else {
					return getPathElt(c.pathElt());
				}
			}
			
			private Node getPathElt(PathEltContext c) {
				if(c.pathMod() != null) {
					Node predicate;
					String symbol = c.pathMod().getText().trim();
					if("?".equals(symbol)) {
						predicate = SH.zeroOrOnePath.asNode();
					}
					else if("+".equals(symbol)) {
						predicate = SH.oneOrMorePath.asNode();
					}
					else {
						predicate = SH.zeroOrMorePath.asNode();
					}
					Node path = NodeFactory.createBlankNode();
					output.triple(Triple.create(path, predicate, getPathPrimary(c.pathPrimary())));
					return path;
				}
				else {
					return getPathPrimary(c.pathPrimary());
				}
			}
			
			private Node getPathPrimary(PathPrimaryContext c) {
				if(c.iri() != null) {
					return getURI(c.iri().getText());
				}
				else {
					return getPath(c.path());
				}
			}

			
			@Override
			public void exitShapeClass(ShapeClassContext ctx) {
				Node nodeShape = getURI(ctx.iri().getText());
				output.triple(Triple.create(nodeShape, RDF.type.asNode(), SH.NodeShape.asNode()));
				output.triple(Triple.create(nodeShape, RDF.type.asNode(), RDFS.Class.asNode()));
				parseConstraints(ctx.nodeShapeBody(), nodeShape);
			}

			
			private Node getURI(String iriRef) {
				if(iriRef.startsWith("<")) {
					return NodeFactory.createURI(iriRef.substring(1, iriRef.length() - 1));
				}
				else {
					String uri = prefixMapping.expandPrefix(iriRef);
					if(uri.equals(iriRef)) {
						throw new RuntimeException("Undefined prefixed name " + iriRef);
					}
					return NodeFactory.createURI(uri);
				}
			}

			
			private void parseConstraints(NodeShapeBodyContext ctx, Node nodeShape) {
				for(ConstraintContext c : ctx.constraint()) {
					if(c.propertyShape() != null) {
						parsePropertyShape(c.propertyShape(), nodeShape);
					}
					else {
						for(NodeOrContext nodeOr : c.nodeOr()) {
							parseNodeOr(nodeOr, nodeShape);
						}
					}
				}
			}
			
			private void parseNodeOr(NodeOrContext ctx, Node shape) {
				if(ctx.nodeNot().size() > 1) {
					List<Node> shapes = new LinkedList<>();
					for(NodeNotContext c : ctx.nodeNot()) {
						Node member = NodeFactory.createBlankNode();
						parseNodeNot(c, member);
						shapes.add(member);
					}
					Node or = getNodeList(shapes.iterator());
					output.triple(Triple.create(shape, SH.or.asNode(), or));
				}
				else {
					for(NodeNotContext c : ctx.nodeNot()) {
						parseNodeNot(c, shape);
					}
				}
			}
			
			private void parseNodeNot(NodeNotContext ctx, Node parentShape) {
				if(ctx.negation() != null) {
					Node not = NodeFactory.createBlankNode();
					output.triple(Triple.create(parentShape, SH.not.asNode(), not));
					parentShape = not;
				}
				parseNodeValue(ctx.nodeValue(), parentShape);
			}

			
			private void parseNodeValue(NodeValueContext nodeValue, Node parentShape) {
				Node predicate = NodeFactory.createURI(SH.NS + nodeValue.nodeParam().getText());
				Node object = getNode(nodeValue.iriOrLiteralOrArray());
				output.triple(Triple.create(parentShape, predicate, object));
			}

			private void parsePropertyShape(PropertyShapeContext ctx, Node parentShape) {
				Node propertyShape = NodeFactory.createBlankNode();
				output.triple(Triple.create(parentShape, SH.property.asNode(), propertyShape));
				output.triple(Triple.create(propertyShape, SH.path.asNode(), getPath(ctx.path())));
				for(PropertyCountContext c : ctx.propertyCount()) {
					parsePropertyCount(c, propertyShape);
				}
				for(PropertyOrContext c : ctx.propertyOr()) {
					parsePropertyOr(c, propertyShape);
				}
			}
			
			
			private void parsePropertyOr(PropertyOrContext ctx, Node shape) {
				if(ctx.propertyNot().size() > 1) {
					List<Node> shapes = new LinkedList<>();
					for(PropertyNotContext c : ctx.propertyNot()) {
						Node member = NodeFactory.createBlankNode();
						parsePropertyNot(c, member);
						shapes.add(member);
					}
					Node or = getNodeList(shapes.iterator());
					output.triple(Triple.create(shape, SH.or.asNode(), or));
				}
				else {
					for(PropertyNotContext c : ctx.propertyNot()) {
						parsePropertyNot(c, shape);
					}
				}
			}
			
			
			private void parsePropertyNot(PropertyNotContext ctx, Node parentShape) {
				if(ctx.negation() != null) {
					Node not = NodeFactory.createBlankNode();
					output.triple(Triple.create(parentShape, SH.not.asNode(), not));
					parentShape = not;
				}
				parsePropertyAtom(ctx.propertyAtom(), parentShape);
			}
			
			
			private void parsePropertyAtom(PropertyAtomContext ctx, Node shape) {
				if(ctx.nodeKind() != null) {
					Node nodeKind = NodeFactory.createURI(SH.NS + ctx.nodeKind().getText());
					output.triple(Triple.create(shape, SH.nodeKind.asNode(), nodeKind));
				}
				if(ctx.propertyType() != null) {
					Node value = getURI(ctx.propertyType().iri().getText());
					if(JenaDatatypes.isSystemDatatype(ResourceFactory.createResource(value.getURI()))) {
						output.triple(Triple.create(shape, SH.datatype.asNode(), value));
					}
					else {
						output.triple(Triple.create(shape, SH.class_.asNode(), value));
					}
				}
				if(ctx.shapeRef() != null) {
					String text = ctx.shapeRef().getText().trim().substring(1); // Skip @
					Node nodeShape = getURI(text);
					output.triple(Triple.create(shape, SH.node.asNode(), nodeShape));
				}
				if(ctx.propertyValue() != null) {
					Node predicate = NodeFactory.createURI(SH.NS + ctx.propertyValue().propertyParam().getText());
					Node object = getNode(ctx.propertyValue().iriOrLiteralOrArray());
					output.triple(Triple.create(shape, predicate, object));
				}
				if(ctx.nodeShapeBody() != null) {
					Node nestedShape = NodeFactory.createBlankNode();
					output.triple(Triple.create(shape, SH.node.asNode(), nestedShape));
					parseConstraints(ctx.nodeShapeBody(), nestedShape);
				}
			}
			
			private void parsePropertyCount(PropertyCountContext ctx, Node propertyShape) {
				Node minCount = NodeFactoryExtra.parseNode(ctx.propertyMinCount().INTEGER().getText());
				if(!"0".equals(minCount.getLiteralLexicalForm())) {
					output.triple(Triple.create(propertyShape, SH.minCount.asNode(), minCount));
				}
				if(!"*".equals(ctx.propertyMaxCount().getText())) {
					Node maxCount = NodeFactoryExtra.parseNode(ctx.propertyMaxCount().INTEGER().getText());
					output.triple(Triple.create(propertyShape, SH.maxCount.asNode(), maxCount));
				}
			}
		};
		walker.walk(listener, docContext);
		
		output.triple(Triple.create(ontology[0], RDF.type.asNode(), OWL.Ontology.asNode()));
		
		imports.add(NodeFactory.createURI(DASH.BASE_URI));
		for(Node imp : imports) {
			output.triple(Triple.create(ontology[0], OWL.imports.asNode(), imp));
		}
	}
}
