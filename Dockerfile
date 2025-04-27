FROM gradle:8.5.0-jdk21 AS build

WORKDIR /app

# Only copy build scripts first (cacheable)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

RUN ./gradlew --no-daemon build -x test || true

COPY . .

RUN ./gradlew clean assemble -x test

# Use a base image with Java
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the built jar into the container
COPY --from=build /app/build/libs/*.jar app.jar

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]