#!/bin/bash

if ! [ -x "$(command -v npm)" ]; then
    printf "\n npm is not installed, install node/npm. See the readme.md \n"
    exit 1
fi

if ! [ -e node_modules/.bin/speccy ]; then
    printf "\n speccy is not installed, trying to install speccy \n"
    npm install
fi

printf '\n Running speccy to merge modular OAS spec files.... \n'
if [ "$1" == "-v" ]; then
	npm run oasMergeVerbose
else
	npm run oasMerge
fi

if ! [ $? -eq 0 ]; then
	printf "\n Error, OAS spec merge failed, run verbose using -v option : 'sbt oasMergeVerbose' or 'npm run oasMergeVerbose' \n"
	exit 1
fi

printf '\n Checking for any nested yaml/json files which are not merged... \n'
if [ `grep '\.yaml\|\.json' resources/public/api/conf/2.0/application.yaml -c` -gt 0 ]; then
 	printf "\n Error, found some nested Yaml/Json files which are not merged. \n"
    exit 1
fi