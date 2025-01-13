ARG ARCH_BASE=eclipse-temurin:21-alpine
FROM $ARCH_BASE AS jre-build

WORKDIR /app

ARG VERSION

# Annotations to embed in container
LABEL org.opencontainers.image.title="TopBraid SHACL API"
LABEL org.opencontainers.image.description="SHACL API in Java based on Apache Jena "
LABEL org.opencontainers.image.source=https://github.com/TopQuadrant/shacl
LABEL org.opencontainers.image.licenses=Apache-2.0
LABEL org.opencontainers.image.version=${VERSION}

# BUILD STAGE 1: install minimal Java environment + curl & zip for SHACL API

RUN apk add --no-cache binutils maven curl zip

# Create a custom Java runtime
RUN $JAVA_HOME/bin/jlink \
         --add-modules java.base,java.compiler,java.desktop,java.management,java.naming,java.net.http,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql,java.xml.crypto,jdk.unsupported \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /javaruntime

# Compile with maven, extract binaries and copy into image
COPY . /app
RUN mvn versions:set -DnewVersion=${VERSION} && mvn package -Dmaven.test.skip=true
RUN unzip target/shacl-${VERSION}-bin.zip -d /app/

# BUILD STAGE 2: keep only Java and SHACL

FROM alpine:3.21.2

ARG VERSION

ENV JAVA_HOME=/usr
ENV PATH="/app/shacl-${VERSION}/bin:${PATH}"

COPY --from=jre-build /javaruntime $JAVA_HOME
COPY --chmod=0755 --from=jre-build /app/shacl-${VERSION} /app/shacl-${VERSION}
COPY --chmod=0755 --from=jre-build "/app/.docker/entrypoint.sh" "/entrypoint.sh"
ENTRYPOINT ["/entrypoint.sh"]
