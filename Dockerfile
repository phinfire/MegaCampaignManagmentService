# ====== BUILD STAGE ======
FROM eclipse-temurin:20-jdk AS build
WORKDIR /build
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:20-jre
WORKDIR /app
COPY --from=build /build/target/megacampaignmanagmentservice-1.0.jar app.jar
COPY .env .env
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
	CMD curl --fail --silent http://localhost:8080/health | grep 'OK' || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
