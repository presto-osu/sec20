#!/bin/bash

JAR_FILE=./build/libs/sec20-all.jar
DATASET_DIR=./dataset/screen
TRIALS=100

MAIN=MainStats

echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/barometer.xml ${DATASET_DIR}/barometer ${TRIALS}
echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/bible.xml ${DATASET_DIR}/bible ${TRIALS}
echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/dpm.xml ${DATASET_DIR}/dpm ${TRIALS}
echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/drumpads.xml ${DATASET_DIR}/drumpads ${TRIALS}
echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/equibase.xml ${DATASET_DIR}/equibase ${TRIALS}
echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/localtv.xml ${DATASET_DIR}/localtv ${TRIALS}
echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/loctracker.xml ${DATASET_DIR}/loctracker ${TRIALS}
echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/mitula.xml ${DATASET_DIR}/mitula ${TRIALS}
echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/moonphases.xml ${DATASET_DIR}/moonphases ${TRIALS}
echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/parking.xml ${DATASET_DIR}/parking ${TRIALS}
echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/parrot.xml ${DATASET_DIR}/parrot ${TRIALS}
echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/post.xml ${DATASET_DIR}/post ${TRIALS}
echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/quicknews.xml ${DATASET_DIR}/quicknews ${TRIALS}
echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/speedlogic.xml ${DATASET_DIR}/speedlogic ${TRIALS}
echo "==================================: "
java -cp ${JAR_FILE} ${MAIN} ${DATASET_DIR}/vidanta.xml ${DATASET_DIR}/vidanta ${TRIALS}

