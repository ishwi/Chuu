FROM gradle:jre15 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar --no-daemon

FROM openjdk:15-jdk

RUN mkdir /app
CMD bash

COPY --from=build /home/gradle/src/build/libs/*.jar /app/chuu.jar

#ENTRYPOINT ["java","--enable-preview", "-jar","/app/chuu.jar","stop-asking"]
