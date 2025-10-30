FROM openjdk:17-slim

# Install Maven (Debian-based slim image supports apt)
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Cache dependencies first
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -q -DskipTests package

EXPOSE 8080

# Run the Spring Boot application
CMD ["sh", "-c", "java -jar target/*.jar"]
