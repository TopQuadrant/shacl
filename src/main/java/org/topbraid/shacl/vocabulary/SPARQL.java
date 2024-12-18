package org.topbraid.shacl.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary for <a href="http://datashapes.org/sparql">Datashapes SPARQL</a>
 * <p>
 * Automatically generated with TopBraid Composer.
 */
public class SPARQL {

    public final static String BASE_URI = "http://datashapes.org/sparql";

    public final static String NS = BASE_URI + "#";

    public final static String PREFIX = "sparql";


    public final static Resource PrefixDeclaration = ResourceFactory.createResource(NS + "PrefixDeclaration");

    public final static Resource abs = ResourceFactory.createResource(NS + "abs");

    public final static Resource add = ResourceFactory.createResource(NS + "add");

    public final static Resource and = ResourceFactory.createResource(NS + "and");

    public final static Property arg1 = ResourceFactory.createProperty(NS + "arg1");

    public final static Property arg2 = ResourceFactory.createProperty(NS + "arg2");

    public final static Property arg3 = ResourceFactory.createProperty(NS + "arg3");

    public final static Property arg4 = ResourceFactory.createProperty(NS + "arg4");

    public final static Resource bnode = ResourceFactory.createResource(NS + "bnode");

    public final static Resource bound = ResourceFactory.createResource(NS + "bound");

    public final static Resource ceil = ResourceFactory.createResource(NS + "ceil");

    public final static Resource coalesce = ResourceFactory.createResource(NS + "coalesce");

    public final static Resource concat = ResourceFactory.createResource(NS + "concat");

    public final static Resource contains = ResourceFactory.createResource(NS + "contains");

    public final static Resource datatype = ResourceFactory.createResource(NS + "datatype");

    public final static Resource day = ResourceFactory.createResource(NS + "day");

    public final static Resource divide = ResourceFactory.createResource(NS + "divide");

    public final static Resource encode_for_uri = ResourceFactory.createResource(NS + "encode_for_uri");

    public final static Resource eq = ResourceFactory.createResource(NS + "eq");

    public final static Resource floor = ResourceFactory.createResource(NS + "floor");

    public final static Resource ge = ResourceFactory.createResource(NS + "ge");

    public final static Resource gt = ResourceFactory.createResource(NS + "gt");

    public final static Resource hours = ResourceFactory.createResource(NS + "hours");

    public final static Resource if_ = ResourceFactory.createResource(NS + "if");

    public final static Resource in = ResourceFactory.createResource(NS + "in");

    public final static Resource iri = ResourceFactory.createResource(NS + "iri");

    public final static Resource isBlank = ResourceFactory.createResource(NS + "isBlank");

    public final static Resource isIRI = ResourceFactory.createResource(NS + "isIRI");

    public final static Resource isLiteral = ResourceFactory.createResource(NS + "isLiteral");

    public final static Resource isNumeric = ResourceFactory.createResource(NS + "isNumeric");

    public final static Resource isURI = ResourceFactory.createResource(NS + "isURI");

    public final static Resource lang = ResourceFactory.createResource(NS + "lang");

    public final static Resource langMatches = ResourceFactory.createResource(NS + "langMatches");

    public final static Resource lcase = ResourceFactory.createResource(NS + "lcase");

    public final static Resource le = ResourceFactory.createResource(NS + "le");

    public final static Resource lt = ResourceFactory.createResource(NS + "lt");

    public final static Resource md5 = ResourceFactory.createResource(NS + "md5");

    public final static Resource minutes = ResourceFactory.createResource(NS + "minutes");

    public final static Resource month = ResourceFactory.createResource(NS + "month");

    public final static Resource multiply = ResourceFactory.createResource(NS + "multiply");

    public final static Resource ne = ResourceFactory.createResource(NS + "ne");

    public final static Resource not = ResourceFactory.createResource(NS + "not");

    public final static Resource notin = ResourceFactory.createResource(NS + "notin");

    public final static Resource now = ResourceFactory.createResource(NS + "now");

    public final static Resource or = ResourceFactory.createResource(NS + "or");

    public final static Resource rand = ResourceFactory.createResource(NS + "rand");

    public final static Resource regex = ResourceFactory.createResource(NS + "regex");

    public final static Resource replace = ResourceFactory.createResource(NS + "replace");

    public final static Resource round = ResourceFactory.createResource(NS + "round");

    public final static Resource sameTerm = ResourceFactory.createResource(NS + "sameTerm");

    public final static Resource seconds = ResourceFactory.createResource(NS + "seconds");

    public final static Resource sha1 = ResourceFactory.createResource(NS + "sha1");

    public final static Resource sha256 = ResourceFactory.createResource(NS + "sha256");

    public final static Resource sha384 = ResourceFactory.createResource(NS + "sha384");

    public final static Resource sha512 = ResourceFactory.createResource(NS + "sha512");

    public final static Resource str = ResourceFactory.createResource(NS + "str");

    public final static Resource strafter = ResourceFactory.createResource(NS + "strafter");

    public final static Resource strbefore = ResourceFactory.createResource(NS + "strbefore");

    public final static Resource strdt = ResourceFactory.createResource(NS + "strdt");

    public final static Resource strends = ResourceFactory.createResource(NS + "strends");

    public final static Resource strlang = ResourceFactory.createResource(NS + "strlang");

    public final static Resource strlen = ResourceFactory.createResource(NS + "strlen");

    public final static Resource strstarts = ResourceFactory.createResource(NS + "strstarts");

    public final static Resource struuid = ResourceFactory.createResource(NS + "struuid");

    public final static Resource substr = ResourceFactory.createResource(NS + "substr");

    public final static Resource subtract = ResourceFactory.createResource(NS + "subtract");

    public final static Property symbol = ResourceFactory.createProperty(NS + "symbol");

    public final static Resource timezone = ResourceFactory.createResource(NS + "timezone");

    public final static Resource tz = ResourceFactory.createResource(NS + "tz");

    public final static Resource ucase = ResourceFactory.createResource(NS + "ucase");

    public final static Resource unaryminus = ResourceFactory.createResource(NS + "unaryminus");

    public final static Resource unaryplus = ResourceFactory.createResource(NS + "unaryplus");

    public final static Property unlimitedParameters = ResourceFactory.createProperty(NS + "unlimitedParameters");

    public final static Resource uri = ResourceFactory.createResource(NS + "uri");

    public final static Resource uuid = ResourceFactory.createResource(NS + "uuid");

    public final static Resource year = ResourceFactory.createResource(NS + "year");


    public static String getURI() {
        return NS;
    }
}
