FROM eclipse-temurin:11 as jre-build
WORKDIR /app

ARG VERSION

# Annotations to embed in container
LABEL org.opencontainers.image.title="TopBraid SHACL API"
LABEL org.opencontainers.image.description="SHACL API in Java based on Apache Jena "
LABEL org.opencontainers.image.source=https://github.com/TopQuadrant/shacl
LABEL org.opencontainers.image.licenses=Apache-2.0
LABEL org.opencontainers.image.version=${VERSION}

# BUILD STAGE 1: install minimal Java environment + curl & zip for SHACL API

# Create a custom Java runtime
RUN $JAVA_HOME/bin/jlink \
         --add-modules java.base,java.compiler,java.desktop,java.management,java.naming,java.net.http,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql,java.xml.crypto,jdk.unsupported \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /javaruntime

RUN apt-get -y update && \
    apt-get -y install curl zip && \
    rm -rf /var/lib/apt/lists/*

# Download and unpack binary version for appropriate release
RUN curl -s https://repo1.maven.org/maven2/org/topbraid/shacl/${VERSION}/shacl-${VERSION}-bin.zip > shacl.zip
RUN unzip shacl.zip && rm shacl.zip

# BUILD STAGE 2: keep only Java and SHACL

FROM ubuntu:jammy

ARG VERSION

ENV JAVA_HOME=/usr
ENV PATH "/app/shacl-${VERSION}/bin:${PATH}"

COPY --from=jre-build /javaruntime $JAVA_HOME
COPY --chmod=0755 --from=jre-build /app/shacl-${VERSION} /app/shacl-${VERSION}
COPY --chmod=0755 "entrypoint.sh" "/entrypoint.sh"

ENTRYPOINT ["/entrypoint.sh"]