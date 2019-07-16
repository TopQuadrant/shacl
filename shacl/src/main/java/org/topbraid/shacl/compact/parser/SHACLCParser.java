// Generated from SHACLC.g4 by ANTLR 4.4

	package org.topbraid.shacl.compact.parser;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SHACLCParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__55=1, T__54=2, T__53=3, T__52=4, T__51=5, T__50=6, T__49=7, T__48=8, 
		T__47=9, T__46=10, T__45=11, T__44=12, T__43=13, T__42=14, T__41=15, T__40=16, 
		T__39=17, T__38=18, T__37=19, T__36=20, T__35=21, T__34=22, T__33=23, 
		T__32=24, T__31=25, T__30=26, T__29=27, T__28=28, T__27=29, T__26=30, 
		T__25=31, T__24=32, T__23=33, T__22=34, T__21=35, T__20=36, T__19=37, 
		T__18=38, T__17=39, T__16=40, T__15=41, T__14=42, T__13=43, T__12=44, 
		T__11=45, T__10=46, T__9=47, T__8=48, T__7=49, T__6=50, T__5=51, T__4=52, 
		T__3=53, T__2=54, T__1=55, T__0=56, KW_BASE=57, KW_IMPORTS=58, KW_PREFIX=59, 
		KW_SHAPE_CLASS=60, KW_SHAPE=61, KW_TRUE=62, KW_FALSE=63, PASS=64, COMMENT=65, 
		IRIREF=66, PNAME_NS=67, PNAME_LN=68, ATPNAME_NS=69, ATPNAME_LN=70, LANGTAG=71, 
		INTEGER=72, DECIMAL=73, DOUBLE=74, STRING_LITERAL1=75, STRING_LITERAL2=76, 
		STRING_LITERAL_LONG1=77, STRING_LITERAL_LONG2=78;
	public static final String[] tokenNames = {
		"<INVALID>", "'Literal'", "'^^'", "'{'", "'..'", "'='", "'^'", "'maxLength'", 
		"'qualifiedMaxCount'", "'('", "'deactivated'", "'qualifiedMinCount'", 
		"'maxExclusive'", "'languageIn'", "'minLength'", "'equals'", "'targetObjectsOf'", 
		"']'", "'hasValue'", "'@'", "'BlankNodeOrLiteral'", "'datatype'", "'targetSubjectsOf'", 
		"'qualifiedValueShape'", "'disjoint'", "'message'", "'IRI'", "'+'", "'BlankNodeOrIRI'", 
		"'closed'", "'nodeKind'", "'/'", "'severity'", "'qualifiedValueShapesDisjoint'", 
		"'class'", "'}'", "'?'", "'uniqueLang'", "'maxInclusive'", "'flags'", 
		"'lessThan'", "'*'", "'.'", "'->'", "'minInclusive'", "'targetNode'", 
		"'['", "'ignoredProperties'", "'IRIOrLiteral'", "'|'", "'pattern'", "'!'", 
		"'in'", "')'", "'minExclusive'", "'BlankNode'", "'lessThanOrEquals'", 
		"'BASE'", "'IMPORTS'", "'PREFIX'", "'shapeClass'", "'shape'", "'true'", 
		"'false'", "PASS", "COMMENT", "IRIREF", "PNAME_NS", "PNAME_LN", "ATPNAME_NS", 
		"ATPNAME_LN", "LANGTAG", "INTEGER", "DECIMAL", "DOUBLE", "STRING_LITERAL1", 
		"STRING_LITERAL2", "STRING_LITERAL_LONG1", "STRING_LITERAL_LONG2"
	};
	public static final int
		RULE_shaclDoc = 0, RULE_directive = 1, RULE_baseDecl = 2, RULE_importsDecl = 3, 
		RULE_prefixDecl = 4, RULE_shapeClass = 5, RULE_nodeShape = 6, RULE_nodeShapeBody = 7, 
		RULE_targetClass = 8, RULE_constraint = 9, RULE_nodeOr = 10, RULE_nodeNot = 11, 
		RULE_nodeValue = 12, RULE_propertyShape = 13, RULE_propertyOr = 14, RULE_propertyNot = 15, 
		RULE_propertyAtom = 16, RULE_propertyCount = 17, RULE_propertyMinCount = 18, 
		RULE_propertyMaxCount = 19, RULE_propertyType = 20, RULE_nodeKind = 21, 
		RULE_shapeRef = 22, RULE_propertyValue = 23, RULE_negation = 24, RULE_path = 25, 
		RULE_pathAlternative = 26, RULE_pathSequence = 27, RULE_pathElt = 28, 
		RULE_pathEltOrInverse = 29, RULE_pathInverse = 30, RULE_pathMod = 31, 
		RULE_pathPrimary = 32, RULE_iriOrLiteralOrArray = 33, RULE_iriOrLiteral = 34, 
		RULE_iri = 35, RULE_prefixedName = 36, RULE_literal = 37, RULE_booleanLiteral = 38, 
		RULE_numericLiteral = 39, RULE_rdfLiteral = 40, RULE_datatype = 41, RULE_string = 42, 
		RULE_array = 43, RULE_nodeParam = 44, RULE_propertyParam = 45;
	public static final String[] ruleNames = {
		"shaclDoc", "directive", "baseDecl", "importsDecl", "prefixDecl", "shapeClass", 
		"nodeShape", "nodeShapeBody", "targetClass", "constraint", "nodeOr", "nodeNot", 
		"nodeValue", "propertyShape", "propertyOr", "propertyNot", "propertyAtom", 
		"propertyCount", "propertyMinCount", "propertyMaxCount", "propertyType", 
		"nodeKind", "shapeRef", "propertyValue", "negation", "path", "pathAlternative", 
		"pathSequence", "pathElt", "pathEltOrInverse", "pathInverse", "pathMod", 
		"pathPrimary", "iriOrLiteralOrArray", "iriOrLiteral", "iri", "prefixedName", 
		"literal", "booleanLiteral", "numericLiteral", "rdfLiteral", "datatype", 
		"string", "array", "nodeParam", "propertyParam"
	};

	@Override
	public String getGrammarFileName() { return "SHACLC.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public SHACLCParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ShaclDocContext extends ParserRuleContext {
		public DirectiveContext directive(int i) {
			return getRuleContext(DirectiveContext.class,i);
		}
		public ShapeClassContext shapeClass(int i) {
			return getRuleContext(ShapeClassContext.class,i);
		}
		public List<DirectiveContext> directive() {
			return getRuleContexts(DirectiveContext.class);
		}
		public NodeShapeContext nodeShape(int i) {
			return getRuleContext(NodeShapeContext.class,i);
		}
		public TerminalNode EOF() { return getToken(SHACLCParser.EOF, 0); }
		public List<NodeShapeContext> nodeShape() {
			return getRuleContexts(NodeShapeContext.class);
		}
		public List<ShapeClassContext> shapeClass() {
			return getRuleContexts(ShapeClassContext.class);
		}
		public ShaclDocContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_shaclDoc; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterShaclDoc(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitShaclDoc(this);
		}
	}

	public final ShaclDocContext shaclDoc() throws RecognitionException {
		ShaclDocContext _localctx = new ShaclDocContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_shaclDoc);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(95);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << KW_BASE) | (1L << KW_IMPORTS) | (1L << KW_PREFIX))) != 0)) {
				{
				{
				setState(92); directive();
				}
				}
				setState(97);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(102);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==KW_SHAPE_CLASS || _la==KW_SHAPE) {
				{
				setState(100);
				switch (_input.LA(1)) {
				case KW_SHAPE:
					{
					setState(98); nodeShape();
					}
					break;
				case KW_SHAPE_CLASS:
					{
					setState(99); shapeClass();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(104);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(105); match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DirectiveContext extends ParserRuleContext {
		public BaseDeclContext baseDecl() {
			return getRuleContext(BaseDeclContext.class,0);
		}
		public PrefixDeclContext prefixDecl() {
			return getRuleContext(PrefixDeclContext.class,0);
		}
		public ImportsDeclContext importsDecl() {
			return getRuleContext(ImportsDeclContext.class,0);
		}
		public DirectiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directive; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterDirective(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitDirective(this);
		}
	}

	public final DirectiveContext directive() throws RecognitionException {
		DirectiveContext _localctx = new DirectiveContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_directive);
		try {
			setState(110);
			switch (_input.LA(1)) {
			case KW_BASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(107); baseDecl();
				}
				break;
			case KW_IMPORTS:
				enterOuterAlt(_localctx, 2);
				{
				setState(108); importsDecl();
				}
				break;
			case KW_PREFIX:
				enterOuterAlt(_localctx, 3);
				{
				setState(109); prefixDecl();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BaseDeclContext extends ParserRuleContext {
		public TerminalNode KW_BASE() { return getToken(SHACLCParser.KW_BASE, 0); }
		public TerminalNode IRIREF() { return getToken(SHACLCParser.IRIREF, 0); }
		public BaseDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_baseDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterBaseDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitBaseDecl(this);
		}
	}

	public final BaseDeclContext baseDecl() throws RecognitionException {
		BaseDeclContext _localctx = new BaseDeclContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_baseDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(112); match(KW_BASE);
			setState(113); match(IRIREF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ImportsDeclContext extends ParserRuleContext {
		public TerminalNode IRIREF() { return getToken(SHACLCParser.IRIREF, 0); }
		public TerminalNode KW_IMPORTS() { return getToken(SHACLCParser.KW_IMPORTS, 0); }
		public ImportsDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importsDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterImportsDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitImportsDecl(this);
		}
	}

	public final ImportsDeclContext importsDecl() throws RecognitionException {
		ImportsDeclContext _localctx = new ImportsDeclContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_importsDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(115); match(KW_IMPORTS);
			setState(116); match(IRIREF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PrefixDeclContext extends ParserRuleContext {
		public TerminalNode KW_PREFIX() { return getToken(SHACLCParser.KW_PREFIX, 0); }
		public TerminalNode IRIREF() { return getToken(SHACLCParser.IRIREF, 0); }
		public TerminalNode PNAME_NS() { return getToken(SHACLCParser.PNAME_NS, 0); }
		public PrefixDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prefixDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPrefixDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPrefixDecl(this);
		}
	}

	public final PrefixDeclContext prefixDecl() throws RecognitionException {
		PrefixDeclContext _localctx = new PrefixDeclContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_prefixDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(118); match(KW_PREFIX);
			setState(119); match(PNAME_NS);
			setState(120); match(IRIREF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ShapeClassContext extends ParserRuleContext {
		public IriContext iri() {
			return getRuleContext(IriContext.class,0);
		}
		public TerminalNode KW_SHAPE_CLASS() { return getToken(SHACLCParser.KW_SHAPE_CLASS, 0); }
		public NodeShapeBodyContext nodeShapeBody() {
			return getRuleContext(NodeShapeBodyContext.class,0);
		}
		public ShapeClassContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_shapeClass; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterShapeClass(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitShapeClass(this);
		}
	}

	public final ShapeClassContext shapeClass() throws RecognitionException {
		ShapeClassContext _localctx = new ShapeClassContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_shapeClass);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(122); match(KW_SHAPE_CLASS);
			setState(123); iri();
			setState(124); nodeShapeBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NodeShapeContext extends ParserRuleContext {
		public TerminalNode KW_SHAPE() { return getToken(SHACLCParser.KW_SHAPE, 0); }
		public TargetClassContext targetClass() {
			return getRuleContext(TargetClassContext.class,0);
		}
		public IriContext iri() {
			return getRuleContext(IriContext.class,0);
		}
		public NodeShapeBodyContext nodeShapeBody() {
			return getRuleContext(NodeShapeBodyContext.class,0);
		}
		public NodeShapeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nodeShape; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterNodeShape(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitNodeShape(this);
		}
	}

	public final NodeShapeContext nodeShape() throws RecognitionException {
		NodeShapeContext _localctx = new NodeShapeContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_nodeShape);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(126); match(KW_SHAPE);
			setState(127); iri();
			setState(129);
			_la = _input.LA(1);
			if (_la==T__13) {
				{
				setState(128); targetClass();
				}
			}

			setState(131); nodeShapeBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NodeShapeBodyContext extends ParserRuleContext {
		public List<ConstraintContext> constraint() {
			return getRuleContexts(ConstraintContext.class);
		}
		public ConstraintContext constraint(int i) {
			return getRuleContext(ConstraintContext.class,i);
		}
		public NodeShapeBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nodeShapeBody; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterNodeShapeBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitNodeShapeBody(this);
		}
	}

	public final NodeShapeBodyContext nodeShapeBody() throws RecognitionException {
		NodeShapeBodyContext _localctx = new NodeShapeBodyContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_nodeShapeBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(133); match(T__53);
			setState(137);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 6)) & ~0x3f) == 0 && ((1L << (_la - 6)) & ((1L << (T__50 - 6)) | (1L << (T__49 - 6)) | (1L << (T__47 - 6)) | (1L << (T__46 - 6)) | (1L << (T__44 - 6)) | (1L << (T__43 - 6)) | (1L << (T__42 - 6)) | (1L << (T__41 - 6)) | (1L << (T__40 - 6)) | (1L << (T__38 - 6)) | (1L << (T__35 - 6)) | (1L << (T__34 - 6)) | (1L << (T__32 - 6)) | (1L << (T__31 - 6)) | (1L << (T__27 - 6)) | (1L << (T__26 - 6)) | (1L << (T__24 - 6)) | (1L << (T__22 - 6)) | (1L << (T__18 - 6)) | (1L << (T__17 - 6)) | (1L << (T__12 - 6)) | (1L << (T__11 - 6)) | (1L << (T__9 - 6)) | (1L << (T__6 - 6)) | (1L << (T__5 - 6)) | (1L << (T__4 - 6)) | (1L << (T__2 - 6)) | (1L << (IRIREF - 6)) | (1L << (PNAME_NS - 6)) | (1L << (PNAME_LN - 6)))) != 0)) {
				{
				{
				setState(134); constraint();
				}
				}
				setState(139);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(140); match(T__21);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TargetClassContext extends ParserRuleContext {
		public List<IriContext> iri() {
			return getRuleContexts(IriContext.class);
		}
		public IriContext iri(int i) {
			return getRuleContext(IriContext.class,i);
		}
		public TargetClassContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_targetClass; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterTargetClass(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitTargetClass(this);
		}
	}

	public final TargetClassContext targetClass() throws RecognitionException {
		TargetClassContext _localctx = new TargetClassContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_targetClass);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(142); match(T__13);
			setState(144); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(143); iri();
				}
				}
				setState(146); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( ((((_la - 66)) & ~0x3f) == 0 && ((1L << (_la - 66)) & ((1L << (IRIREF - 66)) | (1L << (PNAME_NS - 66)) | (1L << (PNAME_LN - 66)))) != 0) );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConstraintContext extends ParserRuleContext {
		public PropertyShapeContext propertyShape() {
			return getRuleContext(PropertyShapeContext.class,0);
		}
		public NodeOrContext nodeOr(int i) {
			return getRuleContext(NodeOrContext.class,i);
		}
		public List<NodeOrContext> nodeOr() {
			return getRuleContexts(NodeOrContext.class);
		}
		public ConstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitConstraint(this);
		}
	}

	public final ConstraintContext constraint() throws RecognitionException {
		ConstraintContext _localctx = new ConstraintContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_constraint);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(154);
			switch (_input.LA(1)) {
			case T__49:
			case T__46:
			case T__44:
			case T__43:
			case T__42:
			case T__41:
			case T__40:
			case T__38:
			case T__35:
			case T__34:
			case T__32:
			case T__31:
			case T__27:
			case T__26:
			case T__24:
			case T__22:
			case T__18:
			case T__17:
			case T__12:
			case T__11:
			case T__9:
			case T__6:
			case T__5:
			case T__4:
			case T__2:
				{
				setState(149); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(148); nodeOr();
					}
					}
					setState(151); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__49) | (1L << T__46) | (1L << T__44) | (1L << T__43) | (1L << T__42) | (1L << T__41) | (1L << T__40) | (1L << T__38) | (1L << T__35) | (1L << T__34) | (1L << T__32) | (1L << T__31) | (1L << T__27) | (1L << T__26) | (1L << T__24) | (1L << T__22) | (1L << T__18) | (1L << T__17) | (1L << T__12) | (1L << T__11) | (1L << T__9) | (1L << T__6) | (1L << T__5) | (1L << T__4) | (1L << T__2))) != 0) );
				}
				break;
			case T__50:
			case T__47:
			case IRIREF:
			case PNAME_NS:
			case PNAME_LN:
				{
				setState(153); propertyShape();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(156); match(T__14);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NodeOrContext extends ParserRuleContext {
		public NodeNotContext nodeNot(int i) {
			return getRuleContext(NodeNotContext.class,i);
		}
		public List<NodeNotContext> nodeNot() {
			return getRuleContexts(NodeNotContext.class);
		}
		public NodeOrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nodeOr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterNodeOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitNodeOr(this);
		}
	}

	public final NodeOrContext nodeOr() throws RecognitionException {
		NodeOrContext _localctx = new NodeOrContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_nodeOr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(158); nodeNot();
			setState(163);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__7) {
				{
				{
				setState(159); match(T__7);
				setState(160); nodeNot();
				}
				}
				setState(165);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NodeNotContext extends ParserRuleContext {
		public NegationContext negation() {
			return getRuleContext(NegationContext.class,0);
		}
		public NodeValueContext nodeValue() {
			return getRuleContext(NodeValueContext.class,0);
		}
		public NodeNotContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nodeNot; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterNodeNot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitNodeNot(this);
		}
	}

	public final NodeNotContext nodeNot() throws RecognitionException {
		NodeNotContext _localctx = new NodeNotContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_nodeNot);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(167);
			_la = _input.LA(1);
			if (_la==T__5) {
				{
				setState(166); negation();
				}
			}

			setState(169); nodeValue();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NodeValueContext extends ParserRuleContext {
		public NodeParamContext nodeParam() {
			return getRuleContext(NodeParamContext.class,0);
		}
		public IriOrLiteralOrArrayContext iriOrLiteralOrArray() {
			return getRuleContext(IriOrLiteralOrArrayContext.class,0);
		}
		public NodeValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nodeValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterNodeValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitNodeValue(this);
		}
	}

	public final NodeValueContext nodeValue() throws RecognitionException {
		NodeValueContext _localctx = new NodeValueContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_nodeValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(171); nodeParam();
			setState(172); match(T__51);
			setState(173); iriOrLiteralOrArray();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyShapeContext extends ParserRuleContext {
		public PropertyCountContext propertyCount(int i) {
			return getRuleContext(PropertyCountContext.class,i);
		}
		public List<PropertyOrContext> propertyOr() {
			return getRuleContexts(PropertyOrContext.class);
		}
		public PathContext path() {
			return getRuleContext(PathContext.class,0);
		}
		public List<PropertyCountContext> propertyCount() {
			return getRuleContexts(PropertyCountContext.class);
		}
		public PropertyOrContext propertyOr(int i) {
			return getRuleContext(PropertyOrContext.class,i);
		}
		public PropertyShapeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyShape; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPropertyShape(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPropertyShape(this);
		}
	}

	public final PropertyShapeContext propertyShape() throws RecognitionException {
		PropertyShapeContext _localctx = new PropertyShapeContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_propertyShape);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(175); path();
			setState(180);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__55) | (1L << T__53) | (1L << T__49) | (1L << T__48) | (1L << T__46) | (1L << T__45) | (1L << T__44) | (1L << T__43) | (1L << T__42) | (1L << T__41) | (1L << T__38) | (1L << T__37) | (1L << T__36) | (1L << T__35) | (1L << T__33) | (1L << T__32) | (1L << T__31) | (1L << T__30) | (1L << T__28) | (1L << T__27) | (1L << T__26) | (1L << T__24) | (1L << T__23) | (1L << T__22) | (1L << T__19) | (1L << T__18) | (1L << T__17) | (1L << T__16) | (1L << T__12) | (1L << T__10) | (1L << T__9) | (1L << T__8) | (1L << T__6) | (1L << T__5) | (1L << T__4) | (1L << T__2) | (1L << T__1) | (1L << T__0))) != 0) || ((((_la - 66)) & ~0x3f) == 0 && ((1L << (_la - 66)) & ((1L << (IRIREF - 66)) | (1L << (PNAME_NS - 66)) | (1L << (PNAME_LN - 66)) | (1L << (ATPNAME_NS - 66)) | (1L << (ATPNAME_LN - 66)))) != 0)) {
				{
				setState(178);
				switch (_input.LA(1)) {
				case T__10:
					{
					setState(176); propertyCount();
					}
					break;
				case T__55:
				case T__53:
				case T__49:
				case T__48:
				case T__46:
				case T__45:
				case T__44:
				case T__43:
				case T__42:
				case T__41:
				case T__38:
				case T__37:
				case T__36:
				case T__35:
				case T__33:
				case T__32:
				case T__31:
				case T__30:
				case T__28:
				case T__27:
				case T__26:
				case T__24:
				case T__23:
				case T__22:
				case T__19:
				case T__18:
				case T__17:
				case T__16:
				case T__12:
				case T__9:
				case T__8:
				case T__6:
				case T__5:
				case T__4:
				case T__2:
				case T__1:
				case T__0:
				case IRIREF:
				case PNAME_NS:
				case PNAME_LN:
				case ATPNAME_NS:
				case ATPNAME_LN:
					{
					setState(177); propertyOr();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(182);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyOrContext extends ParserRuleContext {
		public List<PropertyNotContext> propertyNot() {
			return getRuleContexts(PropertyNotContext.class);
		}
		public PropertyNotContext propertyNot(int i) {
			return getRuleContext(PropertyNotContext.class,i);
		}
		public PropertyOrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyOr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPropertyOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPropertyOr(this);
		}
	}

	public final PropertyOrContext propertyOr() throws RecognitionException {
		PropertyOrContext _localctx = new PropertyOrContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_propertyOr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(183); propertyNot();
			setState(188);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__7) {
				{
				{
				setState(184); match(T__7);
				setState(185); propertyNot();
				}
				}
				setState(190);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyNotContext extends ParserRuleContext {
		public NegationContext negation() {
			return getRuleContext(NegationContext.class,0);
		}
		public PropertyAtomContext propertyAtom() {
			return getRuleContext(PropertyAtomContext.class,0);
		}
		public PropertyNotContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyNot; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPropertyNot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPropertyNot(this);
		}
	}

	public final PropertyNotContext propertyNot() throws RecognitionException {
		PropertyNotContext _localctx = new PropertyNotContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_propertyNot);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(192);
			_la = _input.LA(1);
			if (_la==T__5) {
				{
				setState(191); negation();
				}
			}

			setState(194); propertyAtom();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyAtomContext extends ParserRuleContext {
		public ShapeRefContext shapeRef() {
			return getRuleContext(ShapeRefContext.class,0);
		}
		public PropertyTypeContext propertyType() {
			return getRuleContext(PropertyTypeContext.class,0);
		}
		public NodeKindContext nodeKind() {
			return getRuleContext(NodeKindContext.class,0);
		}
		public PropertyValueContext propertyValue() {
			return getRuleContext(PropertyValueContext.class,0);
		}
		public NodeShapeBodyContext nodeShapeBody() {
			return getRuleContext(NodeShapeBodyContext.class,0);
		}
		public PropertyAtomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyAtom; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPropertyAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPropertyAtom(this);
		}
	}

	public final PropertyAtomContext propertyAtom() throws RecognitionException {
		PropertyAtomContext _localctx = new PropertyAtomContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_propertyAtom);
		try {
			setState(201);
			switch (_input.LA(1)) {
			case IRIREF:
			case PNAME_NS:
			case PNAME_LN:
				enterOuterAlt(_localctx, 1);
				{
				setState(196); propertyType();
				}
				break;
			case T__55:
			case T__36:
			case T__30:
			case T__28:
			case T__8:
			case T__1:
				enterOuterAlt(_localctx, 2);
				{
				setState(197); nodeKind();
				}
				break;
			case T__37:
			case ATPNAME_NS:
			case ATPNAME_LN:
				enterOuterAlt(_localctx, 3);
				{
				setState(198); shapeRef();
				}
				break;
			case T__49:
			case T__48:
			case T__46:
			case T__45:
			case T__44:
			case T__43:
			case T__42:
			case T__41:
			case T__38:
			case T__35:
			case T__33:
			case T__32:
			case T__31:
			case T__27:
			case T__26:
			case T__24:
			case T__23:
			case T__22:
			case T__19:
			case T__18:
			case T__17:
			case T__16:
			case T__12:
			case T__9:
			case T__6:
			case T__4:
			case T__2:
			case T__0:
				enterOuterAlt(_localctx, 4);
				{
				setState(199); propertyValue();
				}
				break;
			case T__53:
				enterOuterAlt(_localctx, 5);
				{
				setState(200); nodeShapeBody();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyCountContext extends ParserRuleContext {
		public PropertyMaxCountContext propertyMaxCount() {
			return getRuleContext(PropertyMaxCountContext.class,0);
		}
		public PropertyMinCountContext propertyMinCount() {
			return getRuleContext(PropertyMinCountContext.class,0);
		}
		public PropertyCountContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyCount; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPropertyCount(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPropertyCount(this);
		}
	}

	public final PropertyCountContext propertyCount() throws RecognitionException {
		PropertyCountContext _localctx = new PropertyCountContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_propertyCount);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(203); match(T__10);
			setState(204); propertyMinCount();
			setState(205); match(T__52);
			setState(206); propertyMaxCount();
			setState(207); match(T__39);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyMinCountContext extends ParserRuleContext {
		public TerminalNode INTEGER() { return getToken(SHACLCParser.INTEGER, 0); }
		public PropertyMinCountContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyMinCount; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPropertyMinCount(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPropertyMinCount(this);
		}
	}

	public final PropertyMinCountContext propertyMinCount() throws RecognitionException {
		PropertyMinCountContext _localctx = new PropertyMinCountContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_propertyMinCount);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(209); match(INTEGER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyMaxCountContext extends ParserRuleContext {
		public TerminalNode INTEGER() { return getToken(SHACLCParser.INTEGER, 0); }
		public PropertyMaxCountContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyMaxCount; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPropertyMaxCount(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPropertyMaxCount(this);
		}
	}

	public final PropertyMaxCountContext propertyMaxCount() throws RecognitionException {
		PropertyMaxCountContext _localctx = new PropertyMaxCountContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_propertyMaxCount);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(211);
			_la = _input.LA(1);
			if ( !(_la==T__15 || _la==INTEGER) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyTypeContext extends ParserRuleContext {
		public IriContext iri() {
			return getRuleContext(IriContext.class,0);
		}
		public PropertyTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPropertyType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPropertyType(this);
		}
	}

	public final PropertyTypeContext propertyType() throws RecognitionException {
		PropertyTypeContext _localctx = new PropertyTypeContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_propertyType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(213); iri();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NodeKindContext extends ParserRuleContext {
		public NodeKindContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nodeKind; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterNodeKind(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitNodeKind(this);
		}
	}

	public final NodeKindContext nodeKind() throws RecognitionException {
		NodeKindContext _localctx = new NodeKindContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_nodeKind);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(215);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__55) | (1L << T__36) | (1L << T__30) | (1L << T__28) | (1L << T__8) | (1L << T__1))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ShapeRefContext extends ParserRuleContext {
		public TerminalNode IRIREF() { return getToken(SHACLCParser.IRIREF, 0); }
		public TerminalNode ATPNAME_NS() { return getToken(SHACLCParser.ATPNAME_NS, 0); }
		public TerminalNode ATPNAME_LN() { return getToken(SHACLCParser.ATPNAME_LN, 0); }
		public ShapeRefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_shapeRef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterShapeRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitShapeRef(this);
		}
	}

	public final ShapeRefContext shapeRef() throws RecognitionException {
		ShapeRefContext _localctx = new ShapeRefContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_shapeRef);
		try {
			setState(221);
			switch (_input.LA(1)) {
			case ATPNAME_LN:
				enterOuterAlt(_localctx, 1);
				{
				setState(217); match(ATPNAME_LN);
				}
				break;
			case ATPNAME_NS:
				enterOuterAlt(_localctx, 2);
				{
				setState(218); match(ATPNAME_NS);
				}
				break;
			case T__37:
				enterOuterAlt(_localctx, 3);
				{
				setState(219); match(T__37);
				setState(220); match(IRIREF);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyValueContext extends ParserRuleContext {
		public IriOrLiteralOrArrayContext iriOrLiteralOrArray() {
			return getRuleContext(IriOrLiteralOrArrayContext.class,0);
		}
		public PropertyParamContext propertyParam() {
			return getRuleContext(PropertyParamContext.class,0);
		}
		public PropertyValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPropertyValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPropertyValue(this);
		}
	}

	public final PropertyValueContext propertyValue() throws RecognitionException {
		PropertyValueContext _localctx = new PropertyValueContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_propertyValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(223); propertyParam();
			setState(224); match(T__51);
			setState(225); iriOrLiteralOrArray();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NegationContext extends ParserRuleContext {
		public NegationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_negation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterNegation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitNegation(this);
		}
	}

	public final NegationContext negation() throws RecognitionException {
		NegationContext _localctx = new NegationContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_negation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(227); match(T__5);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PathContext extends ParserRuleContext {
		public PathAlternativeContext pathAlternative() {
			return getRuleContext(PathAlternativeContext.class,0);
		}
		public PathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPath(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPath(this);
		}
	}

	public final PathContext path() throws RecognitionException {
		PathContext _localctx = new PathContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_path);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(229); pathAlternative();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PathAlternativeContext extends ParserRuleContext {
		public PathSequenceContext pathSequence(int i) {
			return getRuleContext(PathSequenceContext.class,i);
		}
		public List<PathSequenceContext> pathSequence() {
			return getRuleContexts(PathSequenceContext.class);
		}
		public PathAlternativeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathAlternative; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPathAlternative(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPathAlternative(this);
		}
	}

	public final PathAlternativeContext pathAlternative() throws RecognitionException {
		PathAlternativeContext _localctx = new PathAlternativeContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_pathAlternative);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(231); pathSequence();
			setState(236);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__7) {
				{
				{
				setState(232); match(T__7);
				setState(233); pathSequence();
				}
				}
				setState(238);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PathSequenceContext extends ParserRuleContext {
		public PathEltOrInverseContext pathEltOrInverse(int i) {
			return getRuleContext(PathEltOrInverseContext.class,i);
		}
		public List<PathEltOrInverseContext> pathEltOrInverse() {
			return getRuleContexts(PathEltOrInverseContext.class);
		}
		public PathSequenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathSequence; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPathSequence(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPathSequence(this);
		}
	}

	public final PathSequenceContext pathSequence() throws RecognitionException {
		PathSequenceContext _localctx = new PathSequenceContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_pathSequence);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(239); pathEltOrInverse();
			setState(244);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__25) {
				{
				{
				setState(240); match(T__25);
				setState(241); pathEltOrInverse();
				}
				}
				setState(246);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PathEltContext extends ParserRuleContext {
		public PathPrimaryContext pathPrimary() {
			return getRuleContext(PathPrimaryContext.class,0);
		}
		public PathModContext pathMod() {
			return getRuleContext(PathModContext.class,0);
		}
		public PathEltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathElt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPathElt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPathElt(this);
		}
	}

	public final PathEltContext pathElt() throws RecognitionException {
		PathEltContext _localctx = new PathEltContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_pathElt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(247); pathPrimary();
			setState(249);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__29) | (1L << T__20) | (1L << T__15))) != 0)) {
				{
				setState(248); pathMod();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PathEltOrInverseContext extends ParserRuleContext {
		public PathEltContext pathElt() {
			return getRuleContext(PathEltContext.class,0);
		}
		public PathInverseContext pathInverse() {
			return getRuleContext(PathInverseContext.class,0);
		}
		public PathEltOrInverseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathEltOrInverse; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPathEltOrInverse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPathEltOrInverse(this);
		}
	}

	public final PathEltOrInverseContext pathEltOrInverse() throws RecognitionException {
		PathEltOrInverseContext _localctx = new PathEltOrInverseContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_pathEltOrInverse);
		try {
			setState(255);
			switch (_input.LA(1)) {
			case T__47:
			case IRIREF:
			case PNAME_NS:
			case PNAME_LN:
				enterOuterAlt(_localctx, 1);
				{
				setState(251); pathElt();
				}
				break;
			case T__50:
				enterOuterAlt(_localctx, 2);
				{
				setState(252); pathInverse();
				setState(253); pathElt();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PathInverseContext extends ParserRuleContext {
		public PathInverseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathInverse; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPathInverse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPathInverse(this);
		}
	}

	public final PathInverseContext pathInverse() throws RecognitionException {
		PathInverseContext _localctx = new PathInverseContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_pathInverse);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(257); match(T__50);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PathModContext extends ParserRuleContext {
		public PathModContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathMod; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPathMod(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPathMod(this);
		}
	}

	public final PathModContext pathMod() throws RecognitionException {
		PathModContext _localctx = new PathModContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_pathMod);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(259);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__29) | (1L << T__20) | (1L << T__15))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PathPrimaryContext extends ParserRuleContext {
		public IriContext iri() {
			return getRuleContext(IriContext.class,0);
		}
		public PathContext path() {
			return getRuleContext(PathContext.class,0);
		}
		public PathPrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathPrimary; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPathPrimary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPathPrimary(this);
		}
	}

	public final PathPrimaryContext pathPrimary() throws RecognitionException {
		PathPrimaryContext _localctx = new PathPrimaryContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_pathPrimary);
		try {
			setState(266);
			switch (_input.LA(1)) {
			case IRIREF:
			case PNAME_NS:
			case PNAME_LN:
				enterOuterAlt(_localctx, 1);
				{
				setState(261); iri();
				}
				break;
			case T__47:
				enterOuterAlt(_localctx, 2);
				{
				setState(262); match(T__47);
				setState(263); path();
				setState(264); match(T__3);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IriOrLiteralOrArrayContext extends ParserRuleContext {
		public ArrayContext array() {
			return getRuleContext(ArrayContext.class,0);
		}
		public IriOrLiteralContext iriOrLiteral() {
			return getRuleContext(IriOrLiteralContext.class,0);
		}
		public IriOrLiteralOrArrayContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_iriOrLiteralOrArray; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterIriOrLiteralOrArray(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitIriOrLiteralOrArray(this);
		}
	}

	public final IriOrLiteralOrArrayContext iriOrLiteralOrArray() throws RecognitionException {
		IriOrLiteralOrArrayContext _localctx = new IriOrLiteralOrArrayContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_iriOrLiteralOrArray);
		try {
			setState(270);
			switch (_input.LA(1)) {
			case KW_TRUE:
			case KW_FALSE:
			case IRIREF:
			case PNAME_NS:
			case PNAME_LN:
			case INTEGER:
			case DECIMAL:
			case DOUBLE:
			case STRING_LITERAL1:
			case STRING_LITERAL2:
			case STRING_LITERAL_LONG1:
			case STRING_LITERAL_LONG2:
				enterOuterAlt(_localctx, 1);
				{
				setState(268); iriOrLiteral();
				}
				break;
			case T__10:
				enterOuterAlt(_localctx, 2);
				{
				setState(269); array();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IriOrLiteralContext extends ParserRuleContext {
		public IriContext iri() {
			return getRuleContext(IriContext.class,0);
		}
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public IriOrLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_iriOrLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterIriOrLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitIriOrLiteral(this);
		}
	}

	public final IriOrLiteralContext iriOrLiteral() throws RecognitionException {
		IriOrLiteralContext _localctx = new IriOrLiteralContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_iriOrLiteral);
		try {
			setState(274);
			switch (_input.LA(1)) {
			case IRIREF:
			case PNAME_NS:
			case PNAME_LN:
				enterOuterAlt(_localctx, 1);
				{
				setState(272); iri();
				}
				break;
			case KW_TRUE:
			case KW_FALSE:
			case INTEGER:
			case DECIMAL:
			case DOUBLE:
			case STRING_LITERAL1:
			case STRING_LITERAL2:
			case STRING_LITERAL_LONG1:
			case STRING_LITERAL_LONG2:
				enterOuterAlt(_localctx, 2);
				{
				setState(273); literal();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IriContext extends ParserRuleContext {
		public TerminalNode IRIREF() { return getToken(SHACLCParser.IRIREF, 0); }
		public PrefixedNameContext prefixedName() {
			return getRuleContext(PrefixedNameContext.class,0);
		}
		public IriContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_iri; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterIri(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitIri(this);
		}
	}

	public final IriContext iri() throws RecognitionException {
		IriContext _localctx = new IriContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_iri);
		try {
			setState(278);
			switch (_input.LA(1)) {
			case IRIREF:
				enterOuterAlt(_localctx, 1);
				{
				setState(276); match(IRIREF);
				}
				break;
			case PNAME_NS:
			case PNAME_LN:
				enterOuterAlt(_localctx, 2);
				{
				setState(277); prefixedName();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PrefixedNameContext extends ParserRuleContext {
		public TerminalNode PNAME_LN() { return getToken(SHACLCParser.PNAME_LN, 0); }
		public TerminalNode PNAME_NS() { return getToken(SHACLCParser.PNAME_NS, 0); }
		public PrefixedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prefixedName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPrefixedName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPrefixedName(this);
		}
	}

	public final PrefixedNameContext prefixedName() throws RecognitionException {
		PrefixedNameContext _localctx = new PrefixedNameContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_prefixedName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(280);
			_la = _input.LA(1);
			if ( !(_la==PNAME_NS || _la==PNAME_LN) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LiteralContext extends ParserRuleContext {
		public NumericLiteralContext numericLiteral() {
			return getRuleContext(NumericLiteralContext.class,0);
		}
		public RdfLiteralContext rdfLiteral() {
			return getRuleContext(RdfLiteralContext.class,0);
		}
		public BooleanLiteralContext booleanLiteral() {
			return getRuleContext(BooleanLiteralContext.class,0);
		}
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitLiteral(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_literal);
		try {
			setState(285);
			switch (_input.LA(1)) {
			case STRING_LITERAL1:
			case STRING_LITERAL2:
			case STRING_LITERAL_LONG1:
			case STRING_LITERAL_LONG2:
				enterOuterAlt(_localctx, 1);
				{
				setState(282); rdfLiteral();
				}
				break;
			case INTEGER:
			case DECIMAL:
			case DOUBLE:
				enterOuterAlt(_localctx, 2);
				{
				setState(283); numericLiteral();
				}
				break;
			case KW_TRUE:
			case KW_FALSE:
				enterOuterAlt(_localctx, 3);
				{
				setState(284); booleanLiteral();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BooleanLiteralContext extends ParserRuleContext {
		public TerminalNode KW_TRUE() { return getToken(SHACLCParser.KW_TRUE, 0); }
		public TerminalNode KW_FALSE() { return getToken(SHACLCParser.KW_FALSE, 0); }
		public BooleanLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterBooleanLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitBooleanLiteral(this);
		}
	}

	public final BooleanLiteralContext booleanLiteral() throws RecognitionException {
		BooleanLiteralContext _localctx = new BooleanLiteralContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_booleanLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(287);
			_la = _input.LA(1);
			if ( !(_la==KW_TRUE || _la==KW_FALSE) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumericLiteralContext extends ParserRuleContext {
		public TerminalNode DECIMAL() { return getToken(SHACLCParser.DECIMAL, 0); }
		public TerminalNode INTEGER() { return getToken(SHACLCParser.INTEGER, 0); }
		public TerminalNode DOUBLE() { return getToken(SHACLCParser.DOUBLE, 0); }
		public NumericLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterNumericLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitNumericLiteral(this);
		}
	}

	public final NumericLiteralContext numericLiteral() throws RecognitionException {
		NumericLiteralContext _localctx = new NumericLiteralContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_numericLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(289);
			_la = _input.LA(1);
			if ( !(((((_la - 72)) & ~0x3f) == 0 && ((1L << (_la - 72)) & ((1L << (INTEGER - 72)) | (1L << (DECIMAL - 72)) | (1L << (DOUBLE - 72)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RdfLiteralContext extends ParserRuleContext {
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public DatatypeContext datatype() {
			return getRuleContext(DatatypeContext.class,0);
		}
		public TerminalNode LANGTAG() { return getToken(SHACLCParser.LANGTAG, 0); }
		public RdfLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rdfLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterRdfLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitRdfLiteral(this);
		}
	}

	public final RdfLiteralContext rdfLiteral() throws RecognitionException {
		RdfLiteralContext _localctx = new RdfLiteralContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_rdfLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(291); string();
			setState(295);
			switch (_input.LA(1)) {
			case LANGTAG:
				{
				setState(292); match(LANGTAG);
				}
				break;
			case T__54:
				{
				setState(293); match(T__54);
				setState(294); datatype();
				}
				break;
			case T__55:
			case T__53:
			case T__49:
			case T__48:
			case T__46:
			case T__45:
			case T__44:
			case T__43:
			case T__42:
			case T__41:
			case T__40:
			case T__39:
			case T__38:
			case T__37:
			case T__36:
			case T__35:
			case T__34:
			case T__33:
			case T__32:
			case T__31:
			case T__30:
			case T__28:
			case T__27:
			case T__26:
			case T__24:
			case T__23:
			case T__22:
			case T__19:
			case T__18:
			case T__17:
			case T__16:
			case T__14:
			case T__12:
			case T__11:
			case T__10:
			case T__9:
			case T__8:
			case T__7:
			case T__6:
			case T__5:
			case T__4:
			case T__2:
			case T__1:
			case T__0:
			case KW_TRUE:
			case KW_FALSE:
			case IRIREF:
			case PNAME_NS:
			case PNAME_LN:
			case ATPNAME_NS:
			case ATPNAME_LN:
			case INTEGER:
			case DECIMAL:
			case DOUBLE:
			case STRING_LITERAL1:
			case STRING_LITERAL2:
			case STRING_LITERAL_LONG1:
			case STRING_LITERAL_LONG2:
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DatatypeContext extends ParserRuleContext {
		public IriContext iri() {
			return getRuleContext(IriContext.class,0);
		}
		public DatatypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_datatype; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterDatatype(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitDatatype(this);
		}
	}

	public final DatatypeContext datatype() throws RecognitionException {
		DatatypeContext _localctx = new DatatypeContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_datatype);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(297); iri();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StringContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL_LONG1() { return getToken(SHACLCParser.STRING_LITERAL_LONG1, 0); }
		public TerminalNode STRING_LITERAL2() { return getToken(SHACLCParser.STRING_LITERAL2, 0); }
		public TerminalNode STRING_LITERAL1() { return getToken(SHACLCParser.STRING_LITERAL1, 0); }
		public TerminalNode STRING_LITERAL_LONG2() { return getToken(SHACLCParser.STRING_LITERAL_LONG2, 0); }
		public StringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitString(this);
		}
	}

	public final StringContext string() throws RecognitionException {
		StringContext _localctx = new StringContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_string);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(299);
			_la = _input.LA(1);
			if ( !(((((_la - 75)) & ~0x3f) == 0 && ((1L << (_la - 75)) & ((1L << (STRING_LITERAL1 - 75)) | (1L << (STRING_LITERAL2 - 75)) | (1L << (STRING_LITERAL_LONG1 - 75)) | (1L << (STRING_LITERAL_LONG2 - 75)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayContext extends ParserRuleContext {
		public IriOrLiteralContext iriOrLiteral(int i) {
			return getRuleContext(IriOrLiteralContext.class,i);
		}
		public List<IriOrLiteralContext> iriOrLiteral() {
			return getRuleContexts(IriOrLiteralContext.class);
		}
		public ArrayContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterArray(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitArray(this);
		}
	}

	public final ArrayContext array() throws RecognitionException {
		ArrayContext _localctx = new ArrayContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_array);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(301); match(T__10);
			setState(305);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 62)) & ~0x3f) == 0 && ((1L << (_la - 62)) & ((1L << (KW_TRUE - 62)) | (1L << (KW_FALSE - 62)) | (1L << (IRIREF - 62)) | (1L << (PNAME_NS - 62)) | (1L << (PNAME_LN - 62)) | (1L << (INTEGER - 62)) | (1L << (DECIMAL - 62)) | (1L << (DOUBLE - 62)) | (1L << (STRING_LITERAL1 - 62)) | (1L << (STRING_LITERAL2 - 62)) | (1L << (STRING_LITERAL_LONG1 - 62)) | (1L << (STRING_LITERAL_LONG2 - 62)))) != 0)) {
				{
				{
				setState(302); iriOrLiteral();
				}
				}
				setState(307);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(308); match(T__39);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NodeParamContext extends ParserRuleContext {
		public NodeParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nodeParam; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterNodeParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitNodeParam(this);
		}
	}

	public final NodeParamContext nodeParam() throws RecognitionException {
		NodeParamContext _localctx = new NodeParamContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_nodeParam);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(310);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__49) | (1L << T__46) | (1L << T__44) | (1L << T__43) | (1L << T__42) | (1L << T__41) | (1L << T__40) | (1L << T__38) | (1L << T__35) | (1L << T__34) | (1L << T__32) | (1L << T__31) | (1L << T__27) | (1L << T__26) | (1L << T__24) | (1L << T__22) | (1L << T__18) | (1L << T__17) | (1L << T__12) | (1L << T__11) | (1L << T__9) | (1L << T__6) | (1L << T__4) | (1L << T__2))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyParamContext extends ParserRuleContext {
		public PropertyParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyParam; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).enterPropertyParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SHACLCListener ) ((SHACLCListener)listener).exitPropertyParam(this);
		}
	}

	public final PropertyParamContext propertyParam() throws RecognitionException {
		PropertyParamContext _localctx = new PropertyParamContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_propertyParam);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(312);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__49) | (1L << T__48) | (1L << T__46) | (1L << T__45) | (1L << T__44) | (1L << T__43) | (1L << T__42) | (1L << T__41) | (1L << T__38) | (1L << T__35) | (1L << T__33) | (1L << T__32) | (1L << T__31) | (1L << T__27) | (1L << T__26) | (1L << T__24) | (1L << T__23) | (1L << T__22) | (1L << T__19) | (1L << T__18) | (1L << T__17) | (1L << T__16) | (1L << T__12) | (1L << T__9) | (1L << T__6) | (1L << T__4) | (1L << T__2) | (1L << T__0))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3P\u013d\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\3\2\7\2`\n\2\f\2\16\2c\13\2\3\2\3\2\7\2g\n\2\f"+
		"\2\16\2j\13\2\3\2\3\2\3\3\3\3\3\3\5\3q\n\3\3\4\3\4\3\4\3\5\3\5\3\5\3\6"+
		"\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\b\3\b\3\b\5\b\u0084\n\b\3\b\3\b\3\t\3\t"+
		"\7\t\u008a\n\t\f\t\16\t\u008d\13\t\3\t\3\t\3\n\3\n\6\n\u0093\n\n\r\n\16"+
		"\n\u0094\3\13\6\13\u0098\n\13\r\13\16\13\u0099\3\13\5\13\u009d\n\13\3"+
		"\13\3\13\3\f\3\f\3\f\7\f\u00a4\n\f\f\f\16\f\u00a7\13\f\3\r\5\r\u00aa\n"+
		"\r\3\r\3\r\3\16\3\16\3\16\3\16\3\17\3\17\3\17\7\17\u00b5\n\17\f\17\16"+
		"\17\u00b8\13\17\3\20\3\20\3\20\7\20\u00bd\n\20\f\20\16\20\u00c0\13\20"+
		"\3\21\5\21\u00c3\n\21\3\21\3\21\3\22\3\22\3\22\3\22\3\22\5\22\u00cc\n"+
		"\22\3\23\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3"+
		"\27\3\30\3\30\3\30\3\30\5\30\u00e0\n\30\3\31\3\31\3\31\3\31\3\32\3\32"+
		"\3\33\3\33\3\34\3\34\3\34\7\34\u00ed\n\34\f\34\16\34\u00f0\13\34\3\35"+
		"\3\35\3\35\7\35\u00f5\n\35\f\35\16\35\u00f8\13\35\3\36\3\36\5\36\u00fc"+
		"\n\36\3\37\3\37\3\37\3\37\5\37\u0102\n\37\3 \3 \3!\3!\3\"\3\"\3\"\3\""+
		"\3\"\5\"\u010d\n\"\3#\3#\5#\u0111\n#\3$\3$\5$\u0115\n$\3%\3%\5%\u0119"+
		"\n%\3&\3&\3\'\3\'\3\'\5\'\u0120\n\'\3(\3(\3)\3)\3*\3*\3*\3*\5*\u012a\n"+
		"*\3+\3+\3,\3,\3-\3-\7-\u0132\n-\f-\16-\u0135\13-\3-\3-\3.\3.\3/\3/\3/"+
		"\2\2\60\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:"+
		"<>@BDFHJLNPRTVXZ\\\2\13\4\2++JJ\b\2\3\3\26\26\34\34\36\36\62\6299\5\2"+
		"\35\35&&++\3\2EF\3\2@A\3\2JL\3\2MP\21\2\t\t\f\f\16\22\24\24\27\30\32\33"+
		"\37 \"\"$$()./\61\61\64\64\66\6688\20\2\t\n\f\21\24\24\27\27\31\33\37"+
		" \"$\'*..\61\61\64\64\66\6688::\u0131\2a\3\2\2\2\4p\3\2\2\2\6r\3\2\2\2"+
		"\bu\3\2\2\2\nx\3\2\2\2\f|\3\2\2\2\16\u0080\3\2\2\2\20\u0087\3\2\2\2\22"+
		"\u0090\3\2\2\2\24\u009c\3\2\2\2\26\u00a0\3\2\2\2\30\u00a9\3\2\2\2\32\u00ad"+
		"\3\2\2\2\34\u00b1\3\2\2\2\36\u00b9\3\2\2\2 \u00c2\3\2\2\2\"\u00cb\3\2"+
		"\2\2$\u00cd\3\2\2\2&\u00d3\3\2\2\2(\u00d5\3\2\2\2*\u00d7\3\2\2\2,\u00d9"+
		"\3\2\2\2.\u00df\3\2\2\2\60\u00e1\3\2\2\2\62\u00e5\3\2\2\2\64\u00e7\3\2"+
		"\2\2\66\u00e9\3\2\2\28\u00f1\3\2\2\2:\u00f9\3\2\2\2<\u0101\3\2\2\2>\u0103"+
		"\3\2\2\2@\u0105\3\2\2\2B\u010c\3\2\2\2D\u0110\3\2\2\2F\u0114\3\2\2\2H"+
		"\u0118\3\2\2\2J\u011a\3\2\2\2L\u011f\3\2\2\2N\u0121\3\2\2\2P\u0123\3\2"+
		"\2\2R\u0125\3\2\2\2T\u012b\3\2\2\2V\u012d\3\2\2\2X\u012f\3\2\2\2Z\u0138"+
		"\3\2\2\2\\\u013a\3\2\2\2^`\5\4\3\2_^\3\2\2\2`c\3\2\2\2a_\3\2\2\2ab\3\2"+
		"\2\2bh\3\2\2\2ca\3\2\2\2dg\5\16\b\2eg\5\f\7\2fd\3\2\2\2fe\3\2\2\2gj\3"+
		"\2\2\2hf\3\2\2\2hi\3\2\2\2ik\3\2\2\2jh\3\2\2\2kl\7\2\2\3l\3\3\2\2\2mq"+
		"\5\6\4\2nq\5\b\5\2oq\5\n\6\2pm\3\2\2\2pn\3\2\2\2po\3\2\2\2q\5\3\2\2\2"+
		"rs\7;\2\2st\7D\2\2t\7\3\2\2\2uv\7<\2\2vw\7D\2\2w\t\3\2\2\2xy\7=\2\2yz"+
		"\7E\2\2z{\7D\2\2{\13\3\2\2\2|}\7>\2\2}~\5H%\2~\177\5\20\t\2\177\r\3\2"+
		"\2\2\u0080\u0081\7?\2\2\u0081\u0083\5H%\2\u0082\u0084\5\22\n\2\u0083\u0082"+
		"\3\2\2\2\u0083\u0084\3\2\2\2\u0084\u0085\3\2\2\2\u0085\u0086\5\20\t\2"+
		"\u0086\17\3\2\2\2\u0087\u008b\7\5\2\2\u0088\u008a\5\24\13\2\u0089\u0088"+
		"\3\2\2\2\u008a\u008d\3\2\2\2\u008b\u0089\3\2\2\2\u008b\u008c\3\2\2\2\u008c"+
		"\u008e\3\2\2\2\u008d\u008b\3\2\2\2\u008e\u008f\7%\2\2\u008f\21\3\2\2\2"+
		"\u0090\u0092\7-\2\2\u0091\u0093\5H%\2\u0092\u0091\3\2\2\2\u0093\u0094"+
		"\3\2\2\2\u0094\u0092\3\2\2\2\u0094\u0095\3\2\2\2\u0095\23\3\2\2\2\u0096"+
		"\u0098\5\26\f\2\u0097\u0096\3\2\2\2\u0098\u0099\3\2\2\2\u0099\u0097\3"+
		"\2\2\2\u0099\u009a\3\2\2\2\u009a\u009d\3\2\2\2\u009b\u009d\5\34\17\2\u009c"+
		"\u0097\3\2\2\2\u009c\u009b\3\2\2\2\u009d\u009e\3\2\2\2\u009e\u009f\7,"+
		"\2\2\u009f\25\3\2\2\2\u00a0\u00a5\5\30\r\2\u00a1\u00a2\7\63\2\2\u00a2"+
		"\u00a4\5\30\r\2\u00a3\u00a1\3\2\2\2\u00a4\u00a7\3\2\2\2\u00a5\u00a3\3"+
		"\2\2\2\u00a5\u00a6\3\2\2\2\u00a6\27\3\2\2\2\u00a7\u00a5\3\2\2\2\u00a8"+
		"\u00aa\5\62\32\2\u00a9\u00a8\3\2\2\2\u00a9\u00aa\3\2\2\2\u00aa\u00ab\3"+
		"\2\2\2\u00ab\u00ac\5\32\16\2\u00ac\31\3\2\2\2\u00ad\u00ae\5Z.\2\u00ae"+
		"\u00af\7\7\2\2\u00af\u00b0\5D#\2\u00b0\33\3\2\2\2\u00b1\u00b6\5\64\33"+
		"\2\u00b2\u00b5\5$\23\2\u00b3\u00b5\5\36\20\2\u00b4\u00b2\3\2\2\2\u00b4"+
		"\u00b3\3\2\2\2\u00b5\u00b8\3\2\2\2\u00b6\u00b4\3\2\2\2\u00b6\u00b7\3\2"+
		"\2\2\u00b7\35\3\2\2\2\u00b8\u00b6\3\2\2\2\u00b9\u00be\5 \21\2\u00ba\u00bb"+
		"\7\63\2\2\u00bb\u00bd\5 \21\2\u00bc\u00ba\3\2\2\2\u00bd\u00c0\3\2\2\2"+
		"\u00be\u00bc\3\2\2\2\u00be\u00bf\3\2\2\2\u00bf\37\3\2\2\2\u00c0\u00be"+
		"\3\2\2\2\u00c1\u00c3\5\62\32\2\u00c2\u00c1\3\2\2\2\u00c2\u00c3\3\2\2\2"+
		"\u00c3\u00c4\3\2\2\2\u00c4\u00c5\5\"\22\2\u00c5!\3\2\2\2\u00c6\u00cc\5"+
		"*\26\2\u00c7\u00cc\5,\27\2\u00c8\u00cc\5.\30\2\u00c9\u00cc\5\60\31\2\u00ca"+
		"\u00cc\5\20\t\2\u00cb\u00c6\3\2\2\2\u00cb\u00c7\3\2\2\2\u00cb\u00c8\3"+
		"\2\2\2\u00cb\u00c9\3\2\2\2\u00cb\u00ca\3\2\2\2\u00cc#\3\2\2\2\u00cd\u00ce"+
		"\7\60\2\2\u00ce\u00cf\5&\24\2\u00cf\u00d0\7\6\2\2\u00d0\u00d1\5(\25\2"+
		"\u00d1\u00d2\7\23\2\2\u00d2%\3\2\2\2\u00d3\u00d4\7J\2\2\u00d4\'\3\2\2"+
		"\2\u00d5\u00d6\t\2\2\2\u00d6)\3\2\2\2\u00d7\u00d8\5H%\2\u00d8+\3\2\2\2"+
		"\u00d9\u00da\t\3\2\2\u00da-\3\2\2\2\u00db\u00e0\7H\2\2\u00dc\u00e0\7G"+
		"\2\2\u00dd\u00de\7\25\2\2\u00de\u00e0\7D\2\2\u00df\u00db\3\2\2\2\u00df"+
		"\u00dc\3\2\2\2\u00df\u00dd\3\2\2\2\u00e0/\3\2\2\2\u00e1\u00e2\5\\/\2\u00e2"+
		"\u00e3\7\7\2\2\u00e3\u00e4\5D#\2\u00e4\61\3\2\2\2\u00e5\u00e6\7\65\2\2"+
		"\u00e6\63\3\2\2\2\u00e7\u00e8\5\66\34\2\u00e8\65\3\2\2\2\u00e9\u00ee\5"+
		"8\35\2\u00ea\u00eb\7\63\2\2\u00eb\u00ed\58\35\2\u00ec\u00ea\3\2\2\2\u00ed"+
		"\u00f0\3\2\2\2\u00ee\u00ec\3\2\2\2\u00ee\u00ef\3\2\2\2\u00ef\67\3\2\2"+
		"\2\u00f0\u00ee\3\2\2\2\u00f1\u00f6\5<\37\2\u00f2\u00f3\7!\2\2\u00f3\u00f5"+
		"\5<\37\2\u00f4\u00f2\3\2\2\2\u00f5\u00f8\3\2\2\2\u00f6\u00f4\3\2\2\2\u00f6"+
		"\u00f7\3\2\2\2\u00f79\3\2\2\2\u00f8\u00f6\3\2\2\2\u00f9\u00fb\5B\"\2\u00fa"+
		"\u00fc\5@!\2\u00fb\u00fa\3\2\2\2\u00fb\u00fc\3\2\2\2\u00fc;\3\2\2\2\u00fd"+
		"\u0102\5:\36\2\u00fe\u00ff\5> \2\u00ff\u0100\5:\36\2\u0100\u0102\3\2\2"+
		"\2\u0101\u00fd\3\2\2\2\u0101\u00fe\3\2\2\2\u0102=\3\2\2\2\u0103\u0104"+
		"\7\b\2\2\u0104?\3\2\2\2\u0105\u0106\t\4\2\2\u0106A\3\2\2\2\u0107\u010d"+
		"\5H%\2\u0108\u0109\7\13\2\2\u0109\u010a\5\64\33\2\u010a\u010b\7\67\2\2"+
		"\u010b\u010d\3\2\2\2\u010c\u0107\3\2\2\2\u010c\u0108\3\2\2\2\u010dC\3"+
		"\2\2\2\u010e\u0111\5F$\2\u010f\u0111\5X-\2\u0110\u010e\3\2\2\2\u0110\u010f"+
		"\3\2\2\2\u0111E\3\2\2\2\u0112\u0115\5H%\2\u0113\u0115\5L\'\2\u0114\u0112"+
		"\3\2\2\2\u0114\u0113\3\2\2\2\u0115G\3\2\2\2\u0116\u0119\7D\2\2\u0117\u0119"+
		"\5J&\2\u0118\u0116\3\2\2\2\u0118\u0117\3\2\2\2\u0119I\3\2\2\2\u011a\u011b"+
		"\t\5\2\2\u011bK\3\2\2\2\u011c\u0120\5R*\2\u011d\u0120\5P)\2\u011e\u0120"+
		"\5N(\2\u011f\u011c\3\2\2\2\u011f\u011d\3\2\2\2\u011f\u011e\3\2\2\2\u0120"+
		"M\3\2\2\2\u0121\u0122\t\6\2\2\u0122O\3\2\2\2\u0123\u0124\t\7\2\2\u0124"+
		"Q\3\2\2\2\u0125\u0129\5V,\2\u0126\u012a\7I\2\2\u0127\u0128\7\4\2\2\u0128"+
		"\u012a\5T+\2\u0129\u0126\3\2\2\2\u0129\u0127\3\2\2\2\u0129\u012a\3\2\2"+
		"\2\u012aS\3\2\2\2\u012b\u012c\5H%\2\u012cU\3\2\2\2\u012d\u012e\t\b\2\2"+
		"\u012eW\3\2\2\2\u012f\u0133\7\60\2\2\u0130\u0132\5F$\2\u0131\u0130\3\2"+
		"\2\2\u0132\u0135\3\2\2\2\u0133\u0131\3\2\2\2\u0133\u0134\3\2\2\2\u0134"+
		"\u0136\3\2\2\2\u0135\u0133\3\2\2\2\u0136\u0137\7\23\2\2\u0137Y\3\2\2\2"+
		"\u0138\u0139\t\t\2\2\u0139[\3\2\2\2\u013a\u013b\t\n\2\2\u013b]\3\2\2\2"+
		"\36afhp\u0083\u008b\u0094\u0099\u009c\u00a5\u00a9\u00b4\u00b6\u00be\u00c2"+
		"\u00cb\u00df\u00ee\u00f6\u00fb\u0101\u010c\u0110\u0114\u0118\u011f\u0129"+
		"\u0133";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}