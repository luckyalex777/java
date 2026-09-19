#!/bin/bash
set -e
mvn clean formatter:format package spotbugs:check pmd:check checkstyle:check formatter:validate install
