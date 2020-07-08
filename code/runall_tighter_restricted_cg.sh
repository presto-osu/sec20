#!/bin/bash

JAR_FILE=./build/libs/sec20-all.jar
DATASET_DIR=./dataset/cg
TYPE=cg
TRIALS=$2
EPSILON=$1
KRATIO=$3

MAIN=MainTighterRestricted

echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/barometer.xml ${DATASET_DIR}/barometer "${TYPE}" "${TRIALS}" "${EPSILON}" "${KRATIO}"

echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/bible.xml ${DATASET_DIR}/bible "${TYPE}" "${TRIALS}" "${EPSILON}" "${KRATIO}"

echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/dpm.xml ${DATASET_DIR}/dpm "${TYPE}" "${TRIALS}" "${EPSILON}" "${KRATIO}"

echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/drumpads.xml ${DATASET_DIR}/drumpads "${TYPE}" "${TRIALS}" "${EPSILON}" "${KRATIO}"

echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/equibase.xml ${DATASET_DIR}/equibase "${TYPE}" "${TRIALS}" "${EPSILON}" "${KRATIO}"

echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/localtv.xml ${DATASET_DIR}/localtv "${TYPE}" "${TRIALS}" "${EPSILON}" "${KRATIO}"

echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/loctracker.xml ${DATASET_DIR}/loctracker "${TYPE}" "${TRIALS}" "${EPSILON}" "${KRATIO}"

echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/mitula.xml ${DATASET_DIR}/mitula "${TYPE}" "${TRIALS}" "${EPSILON}" "${KRATIO}"

echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/moonphases.xml ${DATASET_DIR}/moonphases "${TYPE}" "${TRIALS}" "${EPSILON}" "${KRATIO}"

echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/parking.xml ${DATASET_DIR}/parking "${TYPE}" "${TRIALS}" "${EPSILON}" "${KRATIO}"

echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/parrot.xml ${DATASET_DIR}/parrot "${TYPE}" "${TRIALS}" "${EPSILON}" "${KRATIO}"

echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/post.xml ${DATASET_DIR}/post "${TYPE}" "${TRIALS}" "${EPSILON}" "${KRATIO}"

echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/quicknews.xml ${DATASET_DIR}/quicknews "${TYPE}" "${TRIALS}" "${EPSILON}" "${KRATIO}"

echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/speedlogic.xml ${DATASET_DIR}/speedlogic "${TYPE}" "${TRIALS}" "${EPSILON}" "${KRATIO}"

echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/vidanta.xml ${DATASET_DIR}/vidanta "${TYPE}" "${TRIALS}" "${EPSILON}" "${KRATIO}"
