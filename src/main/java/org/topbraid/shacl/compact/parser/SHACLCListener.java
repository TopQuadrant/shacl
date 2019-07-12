// Generated from SHACLC.g4 by ANTLR 4.4

	package org.topbraid.shacl.compact.parser;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SHACLCParser}.
 */
public interface SHACLCListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#propertyMinCount}.
	 * @param ctx the parse tree
	 */
	void enterPropertyMinCount(@NotNull SHACLCParser.PropertyMinCountContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#propertyMinCount}.
	 * @param ctx the parse tree
	 */
	void exitPropertyMinCount(@NotNull SHACLCParser.PropertyMinCountContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#propertyShape}.
	 * @param ctx the parse tree
	 */
	void enterPropertyShape(@NotNull SHACLCParser.PropertyShapeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#propertyShape}.
	 * @param ctx the parse tree
	 */
	void exitPropertyShape(@NotNull SHACLCParser.PropertyShapeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#negation}.
	 * @param ctx the parse tree
	 */
	void enterNegation(@NotNull SHACLCParser.NegationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#negation}.
	 * @param ctx the parse tree
	 */
	void exitNegation(@NotNull SHACLCParser.NegationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#shapeClass}.
	 * @param ctx the parse tree
	 */
	void enterShapeClass(@NotNull SHACLCParser.ShapeClassContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#shapeClass}.
	 * @param ctx the parse tree
	 */
	void exitShapeClass(@NotNull SHACLCParser.ShapeClassContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#string}.
	 * @param ctx the parse tree
	 */
	void enterString(@NotNull SHACLCParser.StringContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#string}.
	 * @param ctx the parse tree
	 */
	void exitString(@NotNull SHACLCParser.StringContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#nodeOr}.
	 * @param ctx the parse tree
	 */
	void enterNodeOr(@NotNull SHACLCParser.NodeOrContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#nodeOr}.
	 * @param ctx the parse tree
	 */
	void exitNodeOr(@NotNull SHACLCParser.NodeOrContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#iriOrLiteralOrArray}.
	 * @param ctx the parse tree
	 */
	void enterIriOrLiteralOrArray(@NotNull SHACLCParser.IriOrLiteralOrArrayContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#iriOrLiteralOrArray}.
	 * @param ctx the parse tree
	 */
	void exitIriOrLiteralOrArray(@NotNull SHACLCParser.IriOrLiteralOrArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#baseDecl}.
	 * @param ctx the parse tree
	 */
	void enterBaseDecl(@NotNull SHACLCParser.BaseDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#baseDecl}.
	 * @param ctx the parse tree
	 */
	void exitBaseDecl(@NotNull SHACLCParser.BaseDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#pathAlternative}.
	 * @param ctx the parse tree
	 */
	void enterPathAlternative(@NotNull SHACLCParser.PathAlternativeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#pathAlternative}.
	 * @param ctx the parse tree
	 */
	void exitPathAlternative(@NotNull SHACLCParser.PathAlternativeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#prefixedName}.
	 * @param ctx the parse tree
	 */
	void enterPrefixedName(@NotNull SHACLCParser.PrefixedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#prefixedName}.
	 * @param ctx the parse tree
	 */
	void exitPrefixedName(@NotNull SHACLCParser.PrefixedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#prefixDecl}.
	 * @param ctx the parse tree
	 */
	void enterPrefixDecl(@NotNull SHACLCParser.PrefixDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#prefixDecl}.
	 * @param ctx the parse tree
	 */
	void exitPrefixDecl(@NotNull SHACLCParser.PrefixDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#pathPrimary}.
	 * @param ctx the parse tree
	 */
	void enterPathPrimary(@NotNull SHACLCParser.PathPrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#pathPrimary}.
	 * @param ctx the parse tree
	 */
	void exitPathPrimary(@NotNull SHACLCParser.PathPrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#propertyCount}.
	 * @param ctx the parse tree
	 */
	void enterPropertyCount(@NotNull SHACLCParser.PropertyCountContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#propertyCount}.
	 * @param ctx the parse tree
	 */
	void exitPropertyCount(@NotNull SHACLCParser.PropertyCountContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#nodeParam}.
	 * @param ctx the parse tree
	 */
	void enterNodeParam(@NotNull SHACLCParser.NodeParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#nodeParam}.
	 * @param ctx the parse tree
	 */
	void exitNodeParam(@NotNull SHACLCParser.NodeParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#pathMod}.
	 * @param ctx the parse tree
	 */
	void enterPathMod(@NotNull SHACLCParser.PathModContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#pathMod}.
	 * @param ctx the parse tree
	 */
	void exitPathMod(@NotNull SHACLCParser.PathModContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(@NotNull SHACLCParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(@NotNull SHACLCParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#path}.
	 * @param ctx the parse tree
	 */
	void enterPath(@NotNull SHACLCParser.PathContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#path}.
	 * @param ctx the parse tree
	 */
	void exitPath(@NotNull SHACLCParser.PathContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#propertyOr}.
	 * @param ctx the parse tree
	 */
	void enterPropertyOr(@NotNull SHACLCParser.PropertyOrContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#propertyOr}.
	 * @param ctx the parse tree
	 */
	void exitPropertyOr(@NotNull SHACLCParser.PropertyOrContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#propertyNot}.
	 * @param ctx the parse tree
	 */
	void enterPropertyNot(@NotNull SHACLCParser.PropertyNotContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#propertyNot}.
	 * @param ctx the parse tree
	 */
	void exitPropertyNot(@NotNull SHACLCParser.PropertyNotContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#datatype}.
	 * @param ctx the parse tree
	 */
	void enterDatatype(@NotNull SHACLCParser.DatatypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#datatype}.
	 * @param ctx the parse tree
	 */
	void exitDatatype(@NotNull SHACLCParser.DatatypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#array}.
	 * @param ctx the parse tree
	 */
	void enterArray(@NotNull SHACLCParser.ArrayContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#array}.
	 * @param ctx the parse tree
	 */
	void exitArray(@NotNull SHACLCParser.ArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#nodeShapeBody}.
	 * @param ctx the parse tree
	 */
	void enterNodeShapeBody(@NotNull SHACLCParser.NodeShapeBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#nodeShapeBody}.
	 * @param ctx the parse tree
	 */
	void exitNodeShapeBody(@NotNull SHACLCParser.NodeShapeBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#targetClass}.
	 * @param ctx the parse tree
	 */
	void enterTargetClass(@NotNull SHACLCParser.TargetClassContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#targetClass}.
	 * @param ctx the parse tree
	 */
	void exitTargetClass(@NotNull SHACLCParser.TargetClassContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#propertyType}.
	 * @param ctx the parse tree
	 */
	void enterPropertyType(@NotNull SHACLCParser.PropertyTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#propertyType}.
	 * @param ctx the parse tree
	 */
	void exitPropertyType(@NotNull SHACLCParser.PropertyTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#propertyParam}.
	 * @param ctx the parse tree
	 */
	void enterPropertyParam(@NotNull SHACLCParser.PropertyParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#propertyParam}.
	 * @param ctx the parse tree
	 */
	void exitPropertyParam(@NotNull SHACLCParser.PropertyParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#nodeKind}.
	 * @param ctx the parse tree
	 */
	void enterNodeKind(@NotNull SHACLCParser.NodeKindContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#nodeKind}.
	 * @param ctx the parse tree
	 */
	void exitNodeKind(@NotNull SHACLCParser.NodeKindContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#pathElt}.
	 * @param ctx the parse tree
	 */
	void enterPathElt(@NotNull SHACLCParser.PathEltContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#pathElt}.
	 * @param ctx the parse tree
	 */
	void exitPathElt(@NotNull SHACLCParser.PathEltContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#rdfLiteral}.
	 * @param ctx the parse tree
	 */
	void enterRdfLiteral(@NotNull SHACLCParser.RdfLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#rdfLiteral}.
	 * @param ctx the parse tree
	 */
	void exitRdfLiteral(@NotNull SHACLCParser.RdfLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#iriOrLiteral}.
	 * @param ctx the parse tree
	 */
	void enterIriOrLiteral(@NotNull SHACLCParser.IriOrLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#iriOrLiteral}.
	 * @param ctx the parse tree
	 */
	void exitIriOrLiteral(@NotNull SHACLCParser.IriOrLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#booleanLiteral}.
	 * @param ctx the parse tree
	 */
	void enterBooleanLiteral(@NotNull SHACLCParser.BooleanLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#booleanLiteral}.
	 * @param ctx the parse tree
	 */
	void exitBooleanLiteral(@NotNull SHACLCParser.BooleanLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#numericLiteral}.
	 * @param ctx the parse tree
	 */
	void enterNumericLiteral(@NotNull SHACLCParser.NumericLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#numericLiteral}.
	 * @param ctx the parse tree
	 */
	void exitNumericLiteral(@NotNull SHACLCParser.NumericLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#iri}.
	 * @param ctx the parse tree
	 */
	void enterIri(@NotNull SHACLCParser.IriContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#iri}.
	 * @param ctx the parse tree
	 */
	void exitIri(@NotNull SHACLCParser.IriContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#pathInverse}.
	 * @param ctx the parse tree
	 */
	void enterPathInverse(@NotNull SHACLCParser.PathInverseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#pathInverse}.
	 * @param ctx the parse tree
	 */
	void exitPathInverse(@NotNull SHACLCParser.PathInverseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#pathEltOrInverse}.
	 * @param ctx the parse tree
	 */
	void enterPathEltOrInverse(@NotNull SHACLCParser.PathEltOrInverseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#pathEltOrInverse}.
	 * @param ctx the parse tree
	 */
	void exitPathEltOrInverse(@NotNull SHACLCParser.PathEltOrInverseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#nodeShape}.
	 * @param ctx the parse tree
	 */
	void enterNodeShape(@NotNull SHACLCParser.NodeShapeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#nodeShape}.
	 * @param ctx the parse tree
	 */
	void exitNodeShape(@NotNull SHACLCParser.NodeShapeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#propertyValue}.
	 * @param ctx the parse tree
	 */
	void enterPropertyValue(@NotNull SHACLCParser.PropertyValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#propertyValue}.
	 * @param ctx the parse tree
	 */
	void exitPropertyValue(@NotNull SHACLCParser.PropertyValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#importsDecl}.
	 * @param ctx the parse tree
	 */
	void enterImportsDecl(@NotNull SHACLCParser.ImportsDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#importsDecl}.
	 * @param ctx the parse tree
	 */
	void exitImportsDecl(@NotNull SHACLCParser.ImportsDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#directive}.
	 * @param ctx the parse tree
	 */
	void enterDirective(@NotNull SHACLCParser.DirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#directive}.
	 * @param ctx the parse tree
	 */
	void exitDirective(@NotNull SHACLCParser.DirectiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#propertyMaxCount}.
	 * @param ctx the parse tree
	 */
	void enterPropertyMaxCount(@NotNull SHACLCParser.PropertyMaxCountContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#propertyMaxCount}.
	 * @param ctx the parse tree
	 */
	void exitPropertyMaxCount(@NotNull SHACLCParser.PropertyMaxCountContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#nodeValue}.
	 * @param ctx the parse tree
	 */
	void enterNodeValue(@NotNull SHACLCParser.NodeValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#nodeValue}.
	 * @param ctx the parse tree
	 */
	void exitNodeValue(@NotNull SHACLCParser.NodeValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#nodeNot}.
	 * @param ctx the parse tree
	 */
	void enterNodeNot(@NotNull SHACLCParser.NodeNotContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#nodeNot}.
	 * @param ctx the parse tree
	 */
	void exitNodeNot(@NotNull SHACLCParser.NodeNotContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#propertyAtom}.
	 * @param ctx the parse tree
	 */
	void enterPropertyAtom(@NotNull SHACLCParser.PropertyAtomContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#propertyAtom}.
	 * @param ctx the parse tree
	 */
	void exitPropertyAtom(@NotNull SHACLCParser.PropertyAtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#shaclDoc}.
	 * @param ctx the parse tree
	 */
	void enterShaclDoc(@NotNull SHACLCParser.ShaclDocContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#shaclDoc}.
	 * @param ctx the parse tree
	 */
	void exitShaclDoc(@NotNull SHACLCParser.ShaclDocContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#pathSequence}.
	 * @param ctx the parse tree
	 */
	void enterPathSequence(@NotNull SHACLCParser.PathSequenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#pathSequence}.
	 * @param ctx the parse tree
	 */
	void exitPathSequence(@NotNull SHACLCParser.PathSequenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#constraint}.
	 * @param ctx the parse tree
	 */
	void enterConstraint(@NotNull SHACLCParser.ConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#constraint}.
	 * @param ctx the parse tree
	 */
	void exitConstraint(@NotNull SHACLCParser.ConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link SHACLCParser#shapeRef}.
	 * @param ctx the parse tree
	 */
	void enterShapeRef(@NotNull SHACLCParser.ShapeRefContext ctx);
	/**
	 * Exit a parse tree produced by {@link SHACLCParser#shapeRef}.
	 * @param ctx the parse tree
	 */
	void exitShapeRef(@NotNull SHACLCParser.ShapeRefContext ctx);
}