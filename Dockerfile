# Use a base image with Java
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the built jar into the container
COPY build/libs/*.jar app.jar

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]