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

package org.topbraid.jenax.util;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to the RDF/RDFS/OWL system triples.
 * <p>
 * TopBraid and this API adds some extra triples (such as declaring
 * superclasses for each system class) that make life easier.
 *
 * @author Holger Knublauch
 */
public class SystemTriples {

    private static Model vocabulary;


    private static void ensureSuperClasses(Resource metaClass, Resource superClass) {
        List<Resource> toAdd = collectMissingSuperClasses(metaClass, superClass);
        for (Resource c : toAdd) {
            vocabulary.add(c, RDFS.subClassOf, superClass);
        }
    }


    private static List<Resource> collectMissingSuperClasses(Resource metaClass,
                                                             Resource superClass) {
        List<Resource> toAdd = new ArrayList<>();
        StmtIterator it = vocabulary.listStatements(null, RDF.type, metaClass);
        while (it.hasNext()) {
            Resource c = it.nextStatement().getSubject();
            if (!c.equals(superClass)) {
                if (c.getProperty(RDFS.subClassOf) == null) {
                    toAdd.add(c);
                }
            }
        }
        return toAdd;
    }


    /**
     * Gets the system ontology (a shared copy).
     *
     * @return the system ontology
     */
    public static synchronized Model getVocabularyModel() {
        if (vocabulary == null) {
            vocabulary = JenaUtil.createDefaultModel();
            org.topbraid.jenax.util.JenaUtil.initNamespaces(vocabulary.getGraph());
            vocabulary.setNsPrefix("xsd", XSD.getURI());
            InputStream ttl = SystemTriples.class.getResourceAsStream("/rdf/system-triples.ttl");
            vocabulary.read(ttl, "urn:x:dummy", FileUtils.langTurtle);
            ensureSuperClasses(RDFS.Class, RDFS.Resource);
            ensureSuperClasses(OWL.Class, OWL.Thing);

            // Remove owl imports rdfs which only causes trouble
            vocabulary.removeAll(null, OWL.imports, null);

            vocabulary.add(OWL.Thing, RDFS.subClassOf, RDFS.Resource);
            vocabulary.add(OWL.inverseOf, RDF.type, OWL.SymmetricProperty);
            vocabulary.add(OWL.equivalentClass, RDF.type, OWL.SymmetricProperty);
            vocabulary.add(OWL.equivalentProperty, RDF.type, OWL.SymmetricProperty);
            vocabulary.add(OWL.equivalentProperty, RDFS.range, RDF.Property);
            vocabulary.add(OWL.differentFrom, RDF.type, OWL.SymmetricProperty);
            vocabulary.add(OWL.sameAs, RDF.type, OWL.SymmetricProperty);
            vocabulary.add(OWL.disjointWith, RDF.type, OWL.SymmetricProperty);
            Resource xml = vocabulary.getResource(RDF.dtXMLLiteral.getURI());
            vocabulary.add(xml, RDFS.subClassOf, RDFS.Resource);
            for (String uri : JenaDatatypes.getDatatypeURIs()) {
                Resource r = vocabulary.getResource(uri);
                if (r.getProperty(RDF.type) == null) {
                    vocabulary.add(r, RDF.type, RDFS.Datatype);
                    vocabulary.add(r, RDFS.subClassOf, RDFS.Literal);
                }
            }

            // vocabulary.add(RDF.HTML, RDFS.label, "HTML");

            // Triples were formally in OWL 1, but dropped from OWL 2
            vocabulary.add(RDFS.comment, RDF.type, OWL.AnnotationProperty);
            vocabulary.add(RDFS.label, RDF.type, OWL.AnnotationProperty);
            vocabulary.add(RDFS.isDefinedBy, RDF.type, OWL.AnnotationProperty);
            vocabulary.add(RDFS.seeAlso, RDF.type, OWL.AnnotationProperty);

            // Add rdfs:labels for XSD types
            for (Resource datatype : vocabulary.listSubjectsWithProperty(RDF.type, RDFS.Datatype).toList()) {
                datatype.addProperty(RDFS.label, datatype.getLocalName());
            }
            vocabulary = JenaUtil.asReadOnlyModel(vocabulary);
        }
        return vocabulary;
    }
}
