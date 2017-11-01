#!/bin/bash

find ./src/com/dkarv/testcases/ -maxdepth 1 -mindepth 1 -printf "%f\n" \
| xargs -I{} ./run.sh {} > /dev/null
