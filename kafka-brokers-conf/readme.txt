docker-compose up -d
docker-compose -f docker-compose.yml up -d
docker ps -a
docker inspect kafka-brokers-conf-kafka-1-1
docker exec -it kafka /bin/sh
cd  /usr/bin

./kafka-topics --create --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1 --topic source_frames_queue --config max.message.bytes=10485880
./kafka-topics --create --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1 --topic shape_frames_queue --config max.message.bytes=10485880
./kafka-topics --create --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1 --topic detections_frames_queue --config max.message.bytes=10485880

docker network ls
docker network connect kafka-brokers-conf_default agitated_shannon
docker network connect kafka-brokers-conf_default angry_morse 
docker network connect kafka-brokers-conf_default stoic_shtern
docker network inspect 

#setting replica.fetch.max.bytes=10485880  in the kafka config files server.properties :
cd /etc/kafka
cat server.properties
echo 'replica.fetch.max.bytes=10485880' >> server.properties



./kafka-topics --list --bootstrap-server kafka:9092
./kafka-configs --bootstrap-server localhost:9092 --alter --entity-type topics --entity-name source_frames_queue --add-config max.message.bytes=10485880
./kafka-configs --bootstrap-server localhost:9092 --entity-type brokers --entity-name 1001 --describe --all



./kafka-topics --bootstrap-server kafka:9092 --list



./kafka-console-consumer --bootstrap-server kafka:9092 --topic source_frames_queue --from-beginning --max-messages 10 --consumer-property max.partition.fetch.bytes=10485880

./kafka-console-producer --bootstrap-server kafka:9092 --topic source_frames_queue

kafka-delete-records --bootstrap-server kafka:9092 --offset-json-file ./offsetfile.json