#!/bin/bash

./gradlew :shadowJar

function runall() {
  epsilon=$1
  t=$2
  trials=$3
  bash runall_baseline_$t.sh $epsilon $trials \
    | tee log/baseline.$t.e$epsilon.log
  bash runall_tighter_restricted_$t.sh $epsilon $trials \
    | tee log/tighter.$t.e$epsilon.log
  bash runall_relaxed_distance_$t.sh $epsilon $trials \
    | tee log/relaxed.$t.e$epsilon.log
}

runall "1" "screen" "100"
runall "1" "cg" "100"
