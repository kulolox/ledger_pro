FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache tzdata curl && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY target/ledger-pro.jar app.jar

RUN mkdir -p /app/data && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
