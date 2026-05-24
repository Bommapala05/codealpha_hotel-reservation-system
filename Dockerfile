FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
RUN javac -d . src/hotel/model/*.java src/hotel/utils/*.java src/hotel/service/*.java src/hotel/Main.java
EXPOSE 8080
CMD ["java", "hotel.Main"]
