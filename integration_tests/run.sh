#!/bin/bash

rm -r result
rm -r result2
mkdir -p target
mkdir -p result

# Compile
find src/com/dkarv/verifier -name "*.java" -print | \
xargs javac -d target || exit 255

find src/com/dkarv/testcases/$1 -name "*.java" -print | \
xargs javac -cp target -d target || exit 255


function run {
    echo "## Run tests with $2" >&2
    java -cp target \
    -javaagent:../jdcallgraph/target/jdcallgraph-0.2-agent.jar=./$2 \
    com/dkarv/testcases/$1/Main
    if java -cp target com/dkarv/testcases/$1/Verification ; then
        echo "Verification of >>  $1  << succeeded" >&2
    else
        echo "Verification of >>  $1  << failed" >&2
        exit 1
    fi
}

run $1 "bytebuddy.ini"
mv result result2
mkdir result

run $1 "javassist.ini"
diff -r result/cg result2/cg
diff -r result/ddg result2/ddg
