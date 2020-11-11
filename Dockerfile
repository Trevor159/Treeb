FROM adoptopenjdk/openjdk11:alpine as build
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x mvnw && ./mvnw clean install -U -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM mwader/static-ffmpeg:4.3.1-2 as ffmpeg

FROM mikenye/youtube-dl:2020.10.25_ytdlc as yt-dl

FROM adoptopenjdk/openjdk11:alpine
EXPOSE 8080
#VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
COPY --from=ffmpeg /ffmpeg /util
COPY --from=ffmpeg /ffprobe /util
COPY --from=yt-dl /usr/local/bin/youtube-dlc /util
RUN mkdir /downloads
ENV PATH_DOWNLOADS /downloads
ENV PATH_FFMPEG /util/ffmpeg
ENV PATH_FFPROBE /util/ffmpeg
ENV PATH_YOUTUBEDL /util/youtube-dlc
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -cp app:app/lib/* gg.trevor.treeb.TreebApplication"]