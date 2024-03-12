
docker build -t corey23/card-object-detection-service-kafka .

docker run --name angry_morse --rm  -p 5003:5003  -d corey23/card-object-detection-service-kafka:latest
docker network connect kafka-brokers-conf_default angry_morse

docker run --name angry_morse --rm -ti -p 5003:5003  -d corey23/card-object-detection-service-kafka:latest /bin/bash


docker cp app/ angry_morse:/source/

uvicorn app.main:carddetection --reload

