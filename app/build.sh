#!/bin/bash
./gradlew clean bootJar
docker build -t rulesapp-compiler -f compiler/Dockerfile .
docker build -t rulesapp-engine -f engine/Dockerfile .
