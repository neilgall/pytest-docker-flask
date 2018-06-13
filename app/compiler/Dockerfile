# Build:
#   gradlew :compiler:assemble
#   docker build -t rulesapp-compiler -f compiler/Dockerfile .

FROM symphonicsoft/openjdkbase:latest

WORKDIR /opt/rulesapp/compiler
COPY compiler/build/libs/rulesapp-compiler-*.jar compiler.jar

CMD java -jar compiler.jar

