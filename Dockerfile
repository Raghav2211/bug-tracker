FROM openjdk:11-jre-slim
VOLUME /tmp
COPY build/libs/bug-tracker-*.jar app.jar
ENTRYPOINT ["java","-server", "-Dfile.encoding=utf-8", "-XX:+ExitOnOutOfMemoryError", "-Djava.security.egd=file:/dev/./urandom","-Duser.timezone=UTC","-jar","/app.jar"]