# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-amazoncorretto-11-alpine

RUN apk add --no-cache \
        chromium \
        chromium-chromedriver \
        font-noto \
        nss \
        ttf-freefont

WORKDIR /workspace

# Resolve dependencies before copying source so normal code edits reuse the
# expensive Maven dependency layer.
COPY pom.xml ./
RUN mvn -B -DskipTests -Papi dependency:resolve

COPY src ./src
RUN mvn -B -DskipTests test-compile
RUN mvn -B -DskipTests -Papi test-compile \
    && mkdir -p reports/logs reports/screenshots

ENV CHROME_BINARY=/usr/bin/chromium-browser \
    CHROMEDRIVER_PATH=/usr/bin/chromedriver

# This image is a test runner. Maven goals/profiles/flags can be supplied
# directly, e.g. `docker run IMAGE test -Papi`.
ENTRYPOINT ["mvn", "-B"]
CMD ["test"]
