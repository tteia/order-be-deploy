# 멀티 스테이지 빌드 방법 사용
# 첫 번째 스테이지
FROM openjdk:11 as stage1

WORKDIR /app

# /app/gradlew 파일로 생성.
COPY gradlew .

# /app/gradle 폴더로 생성.
COPY gradle gradle
COPY src src
COPY build.gradle .
COPY settings.gradle .

RUN chmod 777 gradlew
RUN ./gradlew bootJar

# 두 번째 스테이지
FROM openjdk:11
WORKDIR /app
# stage1 에 있는 jar 를 stage2 에 app.jar 라는 이름으로 카피하겠다 !
COPY --from=stage1 /app/build/libs/*.jar app.jar

# cmd 또는 entrypoint 를 통해 컨테이너를 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

# docker 컨테이너 내에서 밖의 전체 host 를 지칭하는 도메인 : host.docker.internal
# docker run -d -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3306/ordersystem ordersystem:latest

# docker 컨테이너 실행 시 볼륨을 설정할 때에는 -v 옵션 사용.
# docker run -d -p 8081:8080 -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3306/board -v /Users/tteia/Desktop/tmp_logs:/app/logs spring_test:latest