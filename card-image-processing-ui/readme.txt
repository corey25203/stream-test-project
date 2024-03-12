npm install
npm install react-scripts --save
npm start

docker build -t corey23/card-image-processing-ui .
docker run --name nice_pascal -p 3000:3000 -p 8087:8087 -d corey23/card-image-processing-ui:latest

docker cp src/ nice_pascal:/app/src

docker exec -it nice_pascal /bin/sh
#docker network connect kafka-brokers-conf_default nice_pascal

curl --request GET http://agitated_shannon:8080/files 

#json curl example
curl --header "Content-Type: application/json"   --request POST   --data @test_files_path\request.json  someservice:8080/some/path/getservice