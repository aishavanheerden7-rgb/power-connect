# PowerConnect API (Java / Spring Boot)
# Step 1 builds the .jar with Maven, step 2 runs it on a small Java image (without Maven).

# ---------- Step 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q dependency:go-offline          # download the libraries first (cached if pom.xml is unchanged)
COPY src ./src
RUN mvn -q -DskipTests package

# ---------- Step 2: run ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/powerconnect-1.0.jar app.jar
ENV TZ=Africa/Lusaka
# Keep memory small (the server is shared)
ENV JAVA_TOOL_OPTIONS="-Xmx256m"
EXPOSE 8088
CMD ["java", "-jar", "app.jar"]
