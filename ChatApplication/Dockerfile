FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

# Set application timezone to India
ENV TZ=Asia/Kolkata
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Kolkata"

EXPOSE 8080

CMD ["java","-jar","target/ChatApplication-0.0.1-SNAPSHOT.jar"]