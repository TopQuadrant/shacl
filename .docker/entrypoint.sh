#!/bin/sh

set -e

# add conditions to either use "validate" or "infer"
# as commands, otherwise print a help statement

if [ $1 == validate ] ; then
	set -- shaclvalidate.sh "$@"
elif [ $1 == infer ] ; then
	set -- shaclinfer.sh "$@"
else
	cat << EOF
Please use this docker image as follows:
docker run -v /path/to/data:/data IMAGE [COMMAND] [PARAMETERS]
COMMAND:
	validate 
		to run validation
	infer
		to run rule inferencing
PARAMETER:
	-datafile /data/myfile.ttl [MANDATORY]
		input to be validated (only .ttl format supported)
	-shapesfile /data/myshapes.ttl [OPTIONAL]
		shapes for validation (only .ttl format supported)
    -maxiterations 1 [OPTIONAL] - default is 1
        iteratively applies the inference rules until the maximum number of iterations is reached (or no new triples are inferred)
    -validateShapes [OPTIONAL]
        in case you want to include the metashapes (from the tosh namespace in particular)
    -addBlankNodes [OPTIONAL]
        adds the blank nodes to the validation report
    -noImports [OPTIONAL]
        disables the import of external ontologies
EOF
	exit 1
fi

exec "$@"
