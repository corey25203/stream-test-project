FROM corey23/ocv-service-base:simple

ADD ./pom.xml /app
ADD ./src /app/src
WORKDIR /app

ENV APP_PATH /app/

EXPOSE 8080 5000 6565 8083

RUN mvn -U -X -f pom.xml clean package -Dmaven.test.skip=true

ENTRYPOINT ["java","-jar","target/card-image-processing-stream-service-impl-0.0.0.1.jar"]