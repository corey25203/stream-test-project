
docker build -t corey23/card-text-detection-service-kafka . 

docker run --name stoic_shtern --rm  -p 5007:5007  -d corey23/card-text-detection-service-kafka:latest
docker network connect kafka-brokers-conf_default stoic_shtern

docker run --name stoic_shtern --rm -ti -p 5007:5007  -d corey23/card-text-detection-service-kafka:latest /bin/bash

docker cp app/ stoic_shtern:/source/
uvicorn app.main:textdetection --reload