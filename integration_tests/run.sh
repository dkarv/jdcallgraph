#!/bin/bash

rm -r result
mkdir -p target
mkdir -p result

find src/com/dkarv/verifier -name "*.java" -print | xargs javac -d target
find src/com/dkarv/testcases/$1 -name "*.java" -print | xargs javac -cp target -d target

java -cp target \
-javaagent:../jdcallgraph/target/jdcallgraph-0.2-agent.jar=./config.ini \
com/dkarv/testcases/$1/Main

if java -cp target com/dkarv/testcases/$1/Verification ; then
    echo "Verification of >>  $1  << succeeded" >&2
else
    echo "Verification of >>  $1  << failed" >&2
fi
