#!/bin/bash

t=$1
f=$2
c=$3

echo "--------- baseline"
cat log/baseline.$t.e1 | grep $f | cut -f $3
echo "--------- tighter"
cat log/tighter.$t.e1 | grep $f | cut -f $3
echo "--------- relaxed"
cat log/relaxed.$t.e1 | grep $f | cut -f $3
