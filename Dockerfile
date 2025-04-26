# Use a Java image
FROM gradle:8.5.0-jdk21 AS build

# Set workdir
WORKDIR /app

# Copy everything
COPY . .

# Build the application (this creates build/libs/*.jar)
RUN ./gradlew build

# Use a base image with Java
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the built jar into the container
COPY --from=build /app/build/libs/*.jar app.jar

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]