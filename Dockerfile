FROM openjdk:8-jre
ADD build/libs/service-device-discovery*.jar app.jar
COPY application.yml /application.yml
EXPOSE 46002
RUN apt-get update -qq && apt-get -y install arping
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

