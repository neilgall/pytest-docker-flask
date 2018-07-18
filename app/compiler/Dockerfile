# Build:
#   gradlew :compiler:assemble
#   docker build -t rulesapp-compiler -f compiler/Dockerfile .

FROM openjdk:latest

WORKDIR /opt/rulesapp/compiler
COPY build/libs/rulesapp-compiler-*.jar compiler.jar

CMD java -jar compiler.jar

