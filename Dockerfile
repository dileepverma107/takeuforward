# Use Maven to build the project
FROM maven:3.8.5-openjdk-17 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# Use a minimal JDK runtime for the final image
FROM openjdk:17.0.1-jdk-slim

# Set the working directory in the final image
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/takuforward-0.0.1-SNAPSHOT.jar /app/takuforward.jar

# Expose the port the application runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "takuforward.jar"]
