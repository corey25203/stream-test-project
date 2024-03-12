./mvn package -DskipTests
java -jar target/card-image-processing-stream-service-impl-1.0.0.jar

docker image build . -t corey23/card-image-processing-stream-service-impl-0.0.1
docker run --name agitated_shannon --rm -ti -p 5000:5000 -p 6565:6565 -p 8080:8080 -p 8083:8083 -d corey23/card-image-processing-stream-service-impl-0.0.1 /bin/bash 
#-e JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,address=*:5005,server=y,suspend=n"

docker network ls
docker network connect kafka-brokers-conf_default agitated_shannon
docker network inspect  kafka-brokers-conf_default

docker cp file.mp4 agitated_shannon:/app/uploads/file.mp4

docker cp target/card-image-processing-stream-service-impl-0.0.0.1.jar agitated_shannon:/app/target/card-image-processing-stream-service-impl-0.0.0.1.jar