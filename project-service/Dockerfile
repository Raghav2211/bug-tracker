FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY build/libs/project-service-*.jar app.jar
ENTRYPOINT ["java","-server", "-Dfile.encoding=utf-8", "-XX:+ExitOnOutOfMemoryError", "-Djava.security.egd=file:/dev/./urandom","-Duser.timezone=UTC","-jar","/app.jar"]