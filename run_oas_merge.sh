#!/bin/bash

if [ "$#" -lt 1 ]
then
    echo "Usage: ./run_oas_merge.sh <<version_no>> [-c] [-v]. Example ./run_oas_merge.sh 2.0  -c -v"
    exit 1
fi

if ! [[ $1 == "1.0" || $1 == "2.0" ]]
then
    printf "\n Only verion no supported for now are 1.0 or 2.0  \n"
    exit 1
fi

if ! [ -x "$(command -v npm)" ]
then
    printf "\n npm is not installed, install node/npm. See the readme.md \n"
    exit 1
fi

if ! [ -e node_modules/.bin/speccy ]
then
    printf "\n speccy is not installed, installing speccy\n"
    npm install
fi


if [[ -e target/scala-2.12/classes/public/api/conf/$1/application.yaml &&  ( "$2" == "-c" || "$3" == "-c" ) ]]
then
    printf "\n OAS merged file alreay exists, so not running speccy merge \n"
else 
	printf '\n Running speccy to merge modular OAS spec files.... \n'
	mkdir -p "target/scala-2.12/classes/public/api/conf/$1"
	if [[ "$2" == "-v" || "$3" == "-v" ]]
	then
		VERSION=$1 npm run oasMergeVerbose
	else
		VERSION=$1 npm run oasMerge
	fi
	
	if ! [ $? -eq 0 ]
	then
		printf "\n Error, OAS spec merge failed, run verbose using -v option : 'sbt oasMergeVerbose' or 'VERSION=$1 npm run oasMergeVerbose' \n"
		exit 1
	fi
	
	printf '\n Checking for any nested yaml/json files which are not merged... \n'
	if [ `grep '\.yaml\|\.json' target/scala-2.12/classes/public/api/conf/$1/application.yaml -c` -gt 0 ]
	then
	 	printf "\n Error, found some nested Yaml/Json files which are not merged. \n"
	    exit 1
	fi
fi

