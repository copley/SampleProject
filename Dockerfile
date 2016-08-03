FROM anapsix/alpine-java:jre8

EXPOSE 8080

ARG VERSION
ENV VERSION ${VERSION:-0.0.0-alpha.0}

COPY target/ar-t-${VERSION}-jar-with-dependencies.jar /tmp/sample-service.jar

ENTRYPOINT ["java", "-jar"]

CMD ["/tmp/sample-service.jar"]