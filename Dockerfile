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
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
COPY --from=ffmpeg /ffmpeg /usr/local/bin/ffmpeg
COPY --from=ffmpeg /ffprobe /usr/local/bin/ffprobe
COPY --from=yt-dl /usr/local/bin/youtube-dlc /usr/local/bin/youtube-dlc
RUN mkdir /downloads
ENV PATH_DOWNLOADS /downloads
ENV PATH_FFMPEG /usr/local/bin/ffmpeg
ENV PATH_FFPROBE /usr/local/bin/ffprobe
ENV PATH_YOUTUBEDL /usr/local/bin/youtube-dlc
RUN apk add --update --no-cache python3 && ln -sf python3 /usr/bin/python
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -cp app:app/lib/* gg.trevor.treeb.TreebApplication"]