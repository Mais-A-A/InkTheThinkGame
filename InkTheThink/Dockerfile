# base image for docker build
FROM maven:3.9.9-eclipse-temurin-17 AS build

# working directory inside the container
WORKDIR /app

# copy pom.xml file inside the docker container
COPY pom.xml .
RUN mvn dependency:go-offline

# copy src file inside the docker container, this include the java soruce code and resources
COPY src ./src
# building the application and compile the java code and package the application into jar file
RUN mvn package -DskipTests

# base image for runtime environment
FROM eclipse-temurin:17

WORKDIR /app

#copy jar file that created in build stage
COPY --from=build /app/target/InkTheThink-0.0.1-SNAPSHOT.jar /app/InkTheThink-0.0.1-SNAPSHOT.jar

EXPOSE 8080

#run the jar file when the container starts
ENTRYPOINT ["java", "-jar", "/app/InkTheThink-0.0.1-SNAPSHOT.jar"]

# set server port:8080 as the default argument to the EntryPoint
CMD ["--server.port=8080"]

