package org.topbraid.shacl.compact;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.LangBuilder;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParserRegistry;
import org.apache.jena.riot.RDFWriterRegistry;
import org.apache.jena.riot.WriterGraphRIOT;
import org.apache.jena.riot.WriterGraphRIOTFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.topbraid.shacl.vocabulary.SH;

public class SHACLC {
	
	public final static String langName = "SHACLC";

	public final static Lang lang = LangBuilder.create(langName, "text/shaclc")
            .addAltNames("shaclc")   
            .addFileExtensions("shaclc")
            .build();
	
	
	private final static Map<String,String> defaultPrefixes = new HashMap<>();
	static {
		defaultPrefixes.put("owl", OWL.NS);
		defaultPrefixes.put("rdf", RDF.getURI());
		defaultPrefixes.put("rdfs", RDFS.getURI());
		defaultPrefixes.put(SH.PREFIX, SH.getURI());
		defaultPrefixes.put("xsd", XSD.getURI());
	}
	
	public static Iterable<String> getDefaultPrefixes() {
		return defaultPrefixes.keySet();
	}
	
	public static String getDefaultPrefixURI(String prefix) {
		return defaultPrefixes.get(prefix);
	}

	
	// Installs the SHACL Compact Syntax
	public static void install() {
		RDFLanguages.register(SHACLC.lang);
		RDFParserRegistry.registerLangTriples(SHACLC.lang, (language, profile) -> new SHACLCReader());
		RDFFormat format = new RDFFormat(SHACLC.lang);
		RDFWriterRegistry.register(SHACLC.lang, format);
		RDFWriterRegistry.register(format, new WriterGraphRIOTFactory() {
			
			@Override
			public WriterGraphRIOT create(RDFFormat syntaxForm) {
				return new SHACLCWriter();
			}
		});
		
		// new SHACLCTestRunner().run();
	}
}
