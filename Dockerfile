# 1. OpenJDK 17 이미지 기반
FROM openjdk:17-oracle

# 2. 작업 디렉토리 생성
WORKDIR /app

# 3. Gradle Wrapper와 build.gradle, settings.gradle 복사
COPY gradlew ./
COPY gradle ./gradle/
COPY build.gradle ./
COPY settings.gradle ./

# 4. 의존성 다운로드
RUN chmod +x ./gradlew && ./gradlew dependencies

# 5. 소스 코드 복사
COPY src ./src

# 6. 빌드
RUN ./gradlew build -x test

# 7. JAR 파일 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 8. 로그 디렉토리 생성
RUN mkdir -p /app/logs
VOLUME /app/logs

# 9. 포트 노출
EXPOSE 8080

# 10. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
