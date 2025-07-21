FROM openjdk:17-jdk-slim

WORKDIR /app

# Install Node.js (required for Playwright CLI commands like 'npx')
RUN apt-get update && apt-get install -y curl gnupg && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs

# Install Chromium dependencies first
RUN apt-get update && apt-get install -y \
    libglib2.0-0 \
    libnss3 \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libcups2 \
    libxcomposite1 \
    libxdamage1 \
    libxrandr2 \
    libgbm1 \
    libasound2 \
    libpangocairo-1.0-0 \
    libpango-1.0-0 \
    libgtk-3-0 \
    libx11-xcb1 \
    libxext6 \
    libxfixes3 \
    libxkbcommon0 \
    libatspi2.0-0 \
    libdrm2 \
    libdbus-1-3 \
    libexpat1 \
    libxslt1.1 \
    libwoff1 \
    libevent-2.1-7 \
    libopus0 \
    libflite1 \
    libwebpdemux2 \
    libwebpmux3 \
    libharfbuzz-icu0 \
    libenchant-2-2 \
    libsecret-1-0 \
    libhyphen0 \
    libmanette-0.2-0 \
    libunwind8 \
    libdw1 \
    libegl1 \
    libglx0 \
    libgudev-1.0-0 \
    libgles2 \
    gstreamer1.0-libav


# Install Playwright and only Chromium
RUN npm install -g playwright && playwright install chromium

COPY target/*.jar app.jar
EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
ENTRYPOINT ["java", "-jar", "app.jar"]
