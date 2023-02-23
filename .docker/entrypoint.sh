#!/bin/bash

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
EOF
	exit 1
fi

exec "$@"