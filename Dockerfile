FROM gradle:jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar
#run gradle model:update


FROM openjdk:17



RUN mkdir /app && mkdir /data/ && mkdir /data/cache/ && chmod -R 777 /data
COPY /fonts /usr/share/fonts/
RUN fc-cache -f -v



COPY --from=build /home/gradle/src/build/libs/*.jar /app/chuu.jar
COPY --from=build /home/gradle/src/* /tmp



#ENTRYPOINT ["java","--enable-preview", "-jar","/app/chuu.jar","stop-asking"]
