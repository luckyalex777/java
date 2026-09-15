#!/bin/bash
set -e
if [ -n "$1" ] ; then
    if [ ! -d "$1" ] ; then
        mvn archetype:generate -DgroupId=com.alexswd -DartifactId=$1
    else
        echo Directory $1 exists already
        exit 1
    fi
else
    echo usage $0 project-name
    exit 1
fi
