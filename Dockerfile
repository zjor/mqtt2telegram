FROM openjdk:11

ARG VCS_REF
LABEL maintainer="Sergey Royz <zjor.se@gmail.com>" \
  org.label-schema.vcs-ref=$VCS_REF \
  org.label-schema.vcs-url="git@github.com:zjor/mqtt2telegram.git"

EXPOSE 8080

ADD "target/mqtt2telegram-jar-with-dependencies.jar" "service.jar"

CMD ["sh", "-c", "java -jar service.jar"]