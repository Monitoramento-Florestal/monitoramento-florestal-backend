
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B


COPY src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

# Cria o usuário do sistema para segurança
RUN groupadd --system arbor && useradd --system --gid arbor --home /app arbor

# Copia o JAR garantindo que o usuário arbor seja o dono dele (--chown)
COPY --chown=arbor:arbor --from=build /app/target/*.jar app.jar


USER arbor


EXPOSE 8080


ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]