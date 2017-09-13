#!/bin/bash

dir="$(dirname "$(pwd)")"
config="$(pwd)/config.ini"
mvn "$@" -DargLine="-javaagent:$dir/jdcallgraph/target/jdcallgraph-0.1-agent.jar=$config"
