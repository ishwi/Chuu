FROM gradle:jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar


#run gradle model:update


FROM openjdk:17.0.1-jdk-slim-bullseye

RUN apt-get update && apt-get install wget fontconfig -y


ENV DOCKERIZE_VERSION v0.6.1
RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz


RUN mkdir /app && mkdir /data/ && mkdir /data/cache/ && chmod -R 777 /data
COPY /fonts /usr/share/fonts/
RUN fc-cache -f -v



COPY --from=build /home/gradle/src/build/libs/*.jar /app/chuu.jar
COPY --from=build /home/gradle/src/* /tmp



#ENTRYPOINT ["java","--enable-preview", "-jar","/app/chuu.jar","stop-asking"]
