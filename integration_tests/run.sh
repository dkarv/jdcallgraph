#!/bin/bash

version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "Running on java $version" >&2

rm -r target
rm -r result
mkdir -p target
mkdir -p result

# Compile
echo "Compile verifier"
find src/com/dkarv/verifier -name "*.java" -print | \
xargs javac -d target || exit 255

echo "Compile project"
find src/com/dkarv/testcases/$1 -name "*.java" -print | \
xargs javac -cp target:lib/junit-4.12.jar -d target || exit 255


function run {
    echo "Run $1 with $2"
    java -cp target:lib/junit-4.12.jar \
    -javaagent:../jdcallgraph/target/jdcallgraph-0.2-agent.jar=./$2 \
    com/dkarv/testcases/$1/Main
    if java -cp target com/dkarv/testcases/$1/Verification ; then
        echo "Verification of >>  $1 with $2  << succeeded" >&2
    else
        echo "Verification of >>  $1 with $2  << failed" >&2
        exit 1
    fi
}

run $1 "bytebuddy.ini"

if [[ "$version" < "1.9" ]]; then
    rm -r result2
    mv result result2
    mkdir -p result

    run $1 "javassist.ini"

    if [[ -f "src/com/dkarv/testcases/$1/.nodiff" ]]; then
        diff -r -X src/com/dkarv/testcases/$1/.nodiff result/cg result2/cg >&2
    else
        diff -r result/cg result2/cg >&2
    fi
    # diff -r result/ddg result2/ddg
fi
