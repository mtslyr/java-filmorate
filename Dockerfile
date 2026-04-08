FROM amazoncorretto:21-alpine AS builder
WORKDIR application
COPY target/*.jar app.jar

# распаковываем jar
RUN java -Djarmode=layertools -jar app.jar extract

# заключительный этап
FROM amazoncorretto:21-alpine

# копируем зависимости приложения
COPY --from=builder /application/dependencies/ ./
COPY --from=builder /application/spring-boot-loader/ ./
COPY --from=builder /application/snapshot-dependencies/ ./
COPY --from=builder /application/application ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
