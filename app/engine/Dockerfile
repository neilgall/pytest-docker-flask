# Build:
#   gradlew :engine:assemble
#   docker build -t rulesapp-engine -f engine/Dockerfile .

FROM openjdk:latest

WORKDIR /opt/rulesapp/engine
COPY build/libs/rulesapp-engine-*.jar engine.jar

CMD java -jar engine.jar

