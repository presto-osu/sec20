#!/bin/bash

JAR_FILE=./build/libs/sec20-all.jar
DATASET_DIR=./dataset/screen/
TRIALS=100
USER_SIZE=500
EPSILON=$1
GS=$2

#echo "==================================: "
#java -cp ${JAR_FILE} Attack ${DATASET_DIR}/barometer.xml ${DATASET_DIR}/barometer ${TRIALS} ${USER_SIZE}
#echo "==================================: "
#java -cp ${JAR_FILE} Attack ${DATASET_DIR}/bible.xml ${DATASET_DIR}/bible ${TRIALS} ${USER_SIZE}
#echo "==================================: "
#java -cp ${JAR_FILE} Attack ${DATASET_DIR}/dpm.xml ${DATASET_DIR}/dpm ${TRIALS} ${USER_SIZE}
#echo "==================================: "
#java -cp ${JAR_FILE} Attack ${DATASET_DIR}/drumpads.xml ${DATASET_DIR}/drumpads ${TRIALS} ${USER_SIZE}
#echo "==================================: "
#java -cp ${JAR_FILE} Attack ${DATASET_DIR}/equibase.xml ${DATASET_DIR}/equibase ${TRIALS} ${USER_SIZE}
#echo "==================================: "
#java -cp ${JAR_FILE} Attack ${DATASET_DIR}/localtv.xml ${DATASET_DIR}/localtv ${TRIALS} ${USER_SIZE}
#echo "==================================: "
#java -cp ${JAR_FILE} Attack ${DATASET_DIR}/loctracker.xml ${DATASET_DIR}/loctracker ${TRIALS} ${USER_SIZE}
#echo "==================================: "
#java -cp ${JAR_FILE} Attack ${DATASET_DIR}/mitula.xml ${DATASET_DIR}/mitula ${TRIALS} ${USER_SIZE}
#echo "==================================: "
#java -cp ${JAR_FILE} Attack ${DATASET_DIR}/moonphases.xml ${DATASET_DIR}/moonphases ${TRIALS} ${USER_SIZE}
#echo "==================================: "
java -cp ${JAR_FILE} Attack ${DATASET_DIR}/parking.xml ${DATASET_DIR}/parking ${TRIALS} ${USER_SIZE} ${EPSILON} ${GS}
#echo "==================================: "
#java -cp ${JAR_FILE} Attack ${DATASET_DIR}/parrot.xml ${DATASET_DIR}/parrot ${TRIALS} ${USER_SIZE}
#echo "==================================: "
#java -cp ${JAR_FILE} Attack ${DATASET_DIR}/post.xml ${DATASET_DIR}/post ${TRIALS} ${USER_SIZE}
#echo "==================================: "
#java -cp ${JAR_FILE} Attack ${DATASET_DIR}/quicknews.xml ${DATASET_DIR}/quicknews ${TRIALS} ${USER_SIZE}
#echo "==================================: "
#java -cp ${JAR_FILE} Attack ${DATASET_DIR}/speedlogic.xml ${DATASET_DIR}/speedlogic ${TRIALS} ${USER_SIZE}
#echo "==================================: "
#java -cp ${JAR_FILE} Attack ${DATASET_DIR}/vidanta.xml ${DATASET_DIR}/vidanta ${TRIALS} ${USER_SIZE}
