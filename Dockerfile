FROM openjdk:17
WORKDIR /usr/app
COPY ./build/libs/sensiflow-1.0.0.jar /usr/app
CMD ["java", "-jar", "sensiflow-1.0.0.jar"]