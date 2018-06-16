# Build:
#   gradlew :engine:assemble
#   docker build -t rulesapp-engine -f engine/Dockerfile .

FROM symphonicsoft/openjdkbase:latest

WORKDIR /opt/rulesapp/engine
COPY engine/build/libs/rulesapp-engine-*.jar engine.jar

CMD java -jar engine.jar

