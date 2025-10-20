# --- Build stage: usa imagem com Maven + JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia o pom e baixa dependências (cache de build)
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copia o código e compila
COPY src ./src
RUN mvn -q -DskipTests package

# --- Runtime stage: JRE 21 enxuto
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
