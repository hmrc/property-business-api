#!/bin/bash

if ! [ -x "$(command -v npm)" ]; then
    printf "\nnpm is not installed, install node/npm. See the readme.md\n"
    exit 1
fi

if ! [ -x "$(command -v speccy)" ]; then
    printf "\nspeccy is not installed, trying to install speccy\n"
    npm install
fi

printf '\nRunning speccy to merge modular OAS spec files....\n'
if [ "$1" == "-v" ]; then
	npm run oasMergeVerbose
else
	npm run oasMerge
fi

if ! [ $? -eq 0 ]; then
	printf "Error, OAS spec merge failed, run verbose using -v option : 'sbt oasMergeVerbose' or 'npm run oasMergeVerbose'"
	exit 1
fi

printf '\nChecking for any nested yaml/json files which are not merged...\n'
if [ `grep '\.yaml\|\.json' resources/public/api/conf/2.0/application.yaml -c` -gt 0 ]; then
 	printf "Error, found some nested Yaml/Json files which are not merged."
    exit 1
fi