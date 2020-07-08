#!/bin/bash

./gradlew :shadowJar

for t in "b" "t" "r"
do
  rm log/attack.$t.log
  for v in -5 -4 -3 -2 -1 0 1 2 3 4 5
  do
    ep=$(python -c "import math; print(math.pow(2, $v))")
    echo ================== $ep $t
    bash runall_attack.sh $ep $1 | grep -E "(prior|post|ratio)" | tee -a log/attack.$t.log
  done
done
