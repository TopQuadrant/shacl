var validation = require("./shapes");

/********************************/
/* Examples                     */
/********************************/
var examples = {

    amf1: {
        dataFormat: "application/ld+json",
        data: '{"http://raml.org/vocabularies/shapes#toValidate": {\
          "http://raml.org/vocabularies/shapes/anon#title": ["hey","ho"],\
          "http://raml.org/vocabularies/shapes/anon#artist": "Antonio Carlos Brasileiro de Almeida Jobim"\
          } }',
        shapesFormat: "application/ld+json",
        shapes: '{\
        "@context":  {\
          "raml-doc": "http://raml.org/vocabularies/document#",\
          "raml-http": "http://raml.org/vocabularies/http#",\
          "raml-shapes": "http://raml.org/vocabularies/shapes#",\
          "hydra": "http://www.w3.org/ns/hydra/core#",\
          "shacl": "http://www.w3.org/ns/shacl#",\
          "schema-org": "http://schema.org/",\
          "xsd": "http://www.w3.org/2001/XMLSchema#"\
        },\
        "@id": "https://mulesoft-labs.github.io/amf-playground/raml/world-music-api/api.raml#/definitions/Entry/items/0",\
        "@type": [\
            "shacl:NodeShape",\
            "shacl:Shape"\
        ],\
        "shacl:targetObjectsOf": {"@id": "raml-shapes:toValidate"},\
        "shacl:property": [\
            {\
                "@id": "https://mulesoft-labs.github.io/amf-playground/raml/world-music-api/api.raml#/definitions/Entry/items/0/property/title",\
                "@type": [\
                    "shacl:PropertyShape",\
                    "shacl:Shape"\
                ],\
                "raml-shapes:propertyLabel": "title",\
                "shacl:dataType": {\
                    "@id": "xsd:string"\
                },\
                "shacl:maxCount": 1,\
                "shacl:minCount": 0,\
                "shacl:path": {\
                    "@id": "http://raml.org/vocabularies/shapes/anon#title"\
                }\
            },\
            {\
                "@id": "https://mulesoft-labs.github.io/amf-playground/raml/world-music-api/api.raml#/definitions/Entry/items/0/property/artist",\
                "@type": [\
                    "shacl:PropertyShape",\
                    "shacl:Shape"\
                ],\
                "raml-shapes:propertyLabel": "artist",\
                "shacl:dataType": {\
                    "@id": "xsd:string"\
                },\
                "shacl:maxCount": 1,\
                "shacl:minCount": 0,\
                "shacl:path": {\
                    "@id": "http://raml.org/vocabularies/shapes/anon#artist"\
                }\
            }\
        ]\
    }'
    },

    personsTTL: {
        dataFormat: "text/turtle",
        data: '@prefix ex: <http://example.org/ns#> .\n\
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n\
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\
@prefix schema: <http://schema.org/> .\n\
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n\
\n\
ex:Bob\n\
    a schema:Person ;\n\
    schema:givenName "Robert" ;\n\
    schema:familyName "Junior" ;\n\
    schema:birthDate "1971-07-07"^^xsd:date ;\n\
    schema:deathDate "1968-09-10"^^xsd:date ;\n\
    schema:address ex:BobsAddress .\n\
\n\
ex:BobsAddress\n\
    schema:streetAddress "1600 Amphitheatre Pkway" ;\n\
    schema:postalCode 9404 .',
        shapesFormat: "text/turtle",
        shapes: '@prefix dash: <http://datashapes.org/dash#> .\n\
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n\
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\
@prefix schema: <http://schema.org/> .\n\
@prefix sh: <http://www.w3.org/ns/shacl#> .\n\
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n\
\n\
schema:PersonShape\n\
    a sh:NodeShape ;\n\
    sh:targetClass schema:Person ;\n\
    sh:property [\n\
        sh:path schema:givenName ;\n\
        sh:datatype xsd:string ;\n\
        sh:name "given name" ;\n\
    ] ;\n\
    sh:property [\n\
        sh:path schema:birthDate ;\n\
        sh:lessThan schema:deathDate ;\n\
        sh:maxCount 1 ;\n\
    ] ;\n\
    sh:property [\n\
        sh:path schema:gender ;\n\
        sh:in ( "female" "male" ) ;\n\
    ] ;\n\
    sh:property [\n\
        sh:path schema:address ;\n\
        sh:node schema:AddressShape ;\n\
    ] .\n\
\n\
schema:AddressShape\n\
    a sh:NodeShape ;\n\
    sh:closed true ;\n\
    sh:property [\n\
        sh:path schema:streetAddress ;\n\
        sh:datatype xsd:string ;\n\
    ] ;\n\
    sh:property [\n\
        sh:path schema:postalCode ;\n\
        sh:or ( [ sh:datatype xsd:string ] [ sh:datatype xsd:integer ] ) ;\n\
        sh:minInclusive 10000 ;\n\
        sh:maxInclusive 99999 ;\n\
    ] .'
    },

    personsJSON: {

        data: '{\n\
    "@context": { "@vocab": "http://schema.org/" },\n\
\n\
    "@id": "http://example.org/ns#Bob",\n\
    "@type": "Person",\n\
    "givenName": "Robert",\n\
    "familyName": "Junior",\n\
    "birthDate": "1971-07-07",\n\
    "deathDate": "1968-09-10",\n\
    "address": {\n\
        "@id": "http://example.org/ns#BobsAddress",\n\
        "streetAddress": "1600 Amphitheatre Pkway",\n\
        "postalCode": 9404\n\
    }\n\
}',
        dataFormat: "application/ld+json",
        shapes: '',
        shapesFormat: "text/turtle"
    }
};
examples.personsJSON.shapes = examples.personsTTL.shapes;
/********************************/
/********************************/


console.log("TESTING THE THING");
console.log(examples.amf1.shapes);
validation.validate(
    examples.amf1.data,
    examples.amf1.dataFormat,
    examples.amf1.shapes,
    examples.amf1.shapesFormat,
    function () {
        console.log("AND BACK AGAIN");
    });
