@echo off

set JAVA_CMD=java
set JAVA_OPTIONS=-Dswing.aatext=true
set JAR_FILE=jwtcodec.jar
set JWT_CODEC_OPTIONS=-listLafs

%JAVA_CMD% %JAVA_OPTIONS% -jar %JAR_FILE% %JWT_CODEC_OPTIONS%
