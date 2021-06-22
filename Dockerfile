FROM gradle:jre16 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar --no-daemon

FROM openjdk:17-ea

RUN mkdir /app
CMD bash

COPY --from=build /home/gradle/src/build/libs/*.jar /app/chuu.jar

#ENTRYPOINT ["java","--enable-preview", "-jar","/app/chuu.jar","stop-asking"]
