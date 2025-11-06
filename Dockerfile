#Isso não é ideal colocar no git ignore mas o meu so roda com o debaixo, ve se o de vcs tambem roda
#se nao so trocar o comentario
#FROM openjdk:17-jdk-slim
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
EXPOSE 8081
CMD ["./mvnw", "spring-boot:run"]