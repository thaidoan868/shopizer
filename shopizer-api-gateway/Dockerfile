# ---- build ----
# working directory is /shopizer-system
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY . /workspace

RUN mvn -q -DskipTests -pl shopizer-api-gateway -am clean package

# ---- run ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update \
  && apt-get install -y --no-install-recommends curl ca-certificates \
  && rm -rf /var/lib/apt/lists/*

RUN useradd -r -u 10001 appuser
USER appuser

COPY --from=build /workspace/shopizer-api-gateway/target/*.jar /app/app.jar

EXPOSE 8080 9000
ENTRYPOINT ["java","-jar","/app/app.jar"]
