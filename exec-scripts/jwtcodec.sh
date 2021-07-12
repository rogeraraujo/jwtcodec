#!/bin/bash

JAVA_CMD=java
JAVA_OPTIONS=-Dswing.aatext=true
JAR_FILE=jwtcodec.jar
JWT_CODEC_OPTIONS=-listLafs

${JAVA_CMD} ${JAVA_OPTIONS} -jar ${JAR_FILE} ${JWT_CODEC_OPTIONS}
