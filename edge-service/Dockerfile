FROM openjdk:17-jdk-slim@sha256:aaa3b3cb27e3e520b8f116863d0580c438ed55ecfa0bc126b41f68c3f62f9774
VOLUME /tmp
COPY build/libs/edge-service-*.jar app.jar
ENTRYPOINT ["java","-server", "-Dfile.encoding=utf-8", "-XX:+ExitOnOutOfMemoryError", "-Djava.security.egd=file:/dev/./urandom","-Duser.timezone=UTC","--enable-preview","-jar","/app.jar"]
