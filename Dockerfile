FROM maven:3.9-eclipse-temurin-11
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:resolve -q
COPY src ./src
COPY keystore.jks .
EXPOSE 8443
CMD ["mvn", "compile", "exec:java", "-q"]
