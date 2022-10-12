FROM adoptopenjdk/openjdk11:ubi

ARG JAR_FILE=/api.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]