# Build stage
FROM maven:3.8-openjdk-17 AS build

WORKDIR /app

# Copy pom.xml first to leverage Docker cache for dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src/ /app/src/

# Build the application
RUN mvn clean package -DskipTests

FROM openjdk:17

ARG VCS_REF
LABEL maintainer="Sergey Royz <zjor.se@gmail.com>" \
  org.label-schema.vcs-ref=$VCS_REF \
  org.label-schema.vcs-url="git@github.com:zjor/mqtt2telegram.git"

EXPOSE 8080

COPY --from=build /app/target/mqtt2telegram-jar-with-dependencies.jar service.jar

CMD ["sh", "-c", "java -jar service.jar"]