#  기본 이미지 설정 (OpenJDK)
FROM eclipse-temurin:17-jdk

#  작업 디렉토리 설정
WORKDIR /app

#   로컬 JAR 파일을 Docker 이미지로 복사 (Gradle의 경우)
COPY build/libs/*SNAPSHOT.jar app.jar

#   애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# 포트 설정 (애플리케이션이 사용하는 포트)
EXPOSE 8080