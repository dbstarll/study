FROM openjdk:8-jre-alpine

VOLUME /tmp

ENV TZ=Asia/Shanghai
RUN set -eux; \
    apk add --no-cache --update tzdata; \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime; \
    echo $TZ > /etc/timezone; \
    chmod ugo+s /bin/ping; \
    addgroup -S -g 1000 study; \
    adduser -S -u 1000 -G study -h /home/study -s /bin/sh -D study

ARG JAR_FILE

COPY --chown=study:study ./target/${JAR_FILE} /home/study/spring-boot.jar

USER study
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/home/study/spring-boot.jar"]