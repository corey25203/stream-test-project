from typing import Union
import base64
import json
# import aiokafka
import json
import torch
import torchvision
import numpy as np
import os
import cv2
from torchvision.models.detection.faster_rcnn import FastRCNNPredictor, FasterRCNN
from contextlib import asynccontextmanager
# from os import environ

from torchvision.ops.boxes import nms
from fastapi import FastAPI

from confluent_kafka import Consumer, Producer, KafkaException, KafkaError
import socket

# from confluent_kafka.schema_registry import SchemaRegistryClient
# from confluent_kafka.serialization import StringSerializer
# from confluent_kafka.schema_registry.avro import AvroSerializer
# from confluent_kafka import SerializingProducer

KAFKA_CONSUMER_TOPIC = os.getenv('KAFKA_CONSUMER_TOPIC', 'source_frames_queue')
KAFKA_PRODUCER_TOPIC = os.getenv('KAFKA_PRODUCER_TOPIC', 'shape_frames_queue')
KAFKA_CONSUMER_GROUP = os.getenv('KAFKA_CONSUMER_GROUP', 'test_group')
KAFKA_BOOTSTRAP_SERVERS = os.getenv('KAFKA_BOOTSTRAP_SERVERS', 'kafka:9092')
KAFKA_CONSUMER_FETCH_MAX_BYTES = os.getenv('KAFKA_CONSUMER_FETCH_MAX_BYTES', 10485880)
KAFKA_CONSUMER_MAX_PARTITION_FETCH_BYTES = os.getenv('KAFKA_CONSUMER_MAX_PARTITION_FETCH_BYTES', 10585880)
KAFKA_PRODUCER_MAX_MESSAGE_BYTES = os.getenv('KAFKA_PRODUCER_MAX_MESSAGE_BYTES', 10585880)

consumer_conf = {
    'bootstrap.servers': KAFKA_BOOTSTRAP_SERVERS,
    'group.id': KAFKA_CONSUMER_GROUP,
    'enable.auto.commit': 'false',
    'auto.offset.reset': 'earliest',
    'max.partition.fetch.bytes': KAFKA_CONSUMER_MAX_PARTITION_FETCH_BYTES,
    'fetch.max.bytes': KAFKA_PRODUCER_MAX_MESSAGE_BYTES
}

consumer = Consumer(consumer_conf)
# consumer_topics = ['source_frames_queue'];
consumer_topics: list[str] = []
consumer_topics.append(KAFKA_CONSUMER_TOPIC);

producer_conf = {
    'bootstrap.servers': KAFKA_BOOTSTRAP_SERVERS,
    # 'max.request.size': 1048588 -librdkafka-> message.max.bytes': 1048588
    'message.max.bytes': KAFKA_PRODUCER_MAX_MESSAGE_BYTES,
    # 'max.partition.fetch.bytes': 104858800,
    'client.id': socket.gethostname()
}

producer = Producer(producer_conf)
producer_topic = KAFKA_PRODUCER_TOPIC

running = True


def create_model2(num_classes, deviceinst, pretrained=False):
    device = torch.device('cuda') if torch.cuda.is_available() else torch.device('cpu')
    model = torchvision.models.detection.fasterrcnn_resnet50_fpn(pretrained=False)
    in_features = model.roi_heads.box_predictor.cls_score.in_features
    model.roi_heads.box_predictor = FastRCNNPredictor(in_features, num_classes)
    model.to(deviceinst)
    model.load_state_dict(torch.load('./fasterrcnn_resnet50_fpn_card_0.pth'))
    model.eval()
    print('FasterRCNN create_model2')
    return model


###############################################################################################################
deviceinst = torch.device('cuda') if torch.cuda.is_available() else torch.device('cpu')
# load the model and the trained weights
fasterRCNNModel: FasterRCNN = create_model2(2, deviceinst, False);

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Load
    print('FasterRCNN load')
    basic_consume_loop(consumer, consumer_topics)
    yield
    # Clean
    print(f"FasterRCNN clean ")


carddetection = FastAPI(lifespan=lifespan)


def shutdown():
    running = False;


# @carddetection.on_event("startup")
# async def startup_event():


# @carddetection.on_event("shutdown")
# async def shutdown_event():


def print_assignment(consumer, partitions):
    print('Assignment:', partitions)


def acked_poll(err, msg):
    if err is not None:
        print("Failed to deliver message: %s: %s" % (str(msg), str(err)))
    else:
        print("Message produced: %s" % (str(msg)))


def basic_consume_loop(consumer, topics):
    try:
        consumer.subscribe(topics, print_assignment)

        while running:
            msg = consumer.poll(timeout=0.0)
            if msg is None: continue

            if msg.error():
                if msg.error().code() == KafkaError._PARTITION_EOF:
                    print(f"PARTITION_EOF: {msg.topic()}, {msg.partition()}, {msg.offset()}")
                elif msg.error():
                    raise KafkaException(msg.error())
            else:
                print(
                    "consumed: ",
                    msg.topic,
                    msg.partition,
                    msg.offset,
                    msg.key,
                    msg.value,
                    msg.timestamp,
                )
                payload = msg.value()
                json_load = json.loads(payload)
                encodedImg = json_load.get("data", "data attr not found")
                encodedImgId = json_load.get("id", "0")
                cropped_images_list, detected = detect_card_shapes(encodedImg, 90)
                print(f"create result : {detected} ")

                for cr_img in cropped_images_list:
                    SourceFrame = {
                        "id": encodedImgId,
                        "streamId": "",
                        "sessionId": "",
                        "data": cr_img,
                        "description": "",
                        "status": "",
                        "dataContentInfo": ""
                    }

                    json_data = json.dumps(SourceFrame)
                    producer.produce(producer_topic, key="key", value=json_data, callback=acked_poll)

                producer.poll()

    finally:
        consumer.close()


def detect_card_shapes(origb64encoded, iou_threshold=0.1, threshold=0.8, scale_percent=70, max_index=3):
    orig = base64.b64decode(origb64encoded)
    img = cv2.imdecode(np.frombuffer(orig, dtype=np.int8), flags=1)

    img_ = img
    img_ = torch.from_numpy(img_).permute(2, 0, 1).unsqueeze(0).to(torch.float).to(deviceinst)
    detected = fasterRCNNModel(img_)
    ind = nms(detected[0]['boxes'], detected[0]['scores'], iou_threshold).detach().cpu().numpy()

    cropped_images_list = []

    for i, box in enumerate(detected[0]['boxes'][ind]):
        if (detected[0]['scores'][i] > threshold) & (i <= max_index):
            # cv2.rectangle(img,
            #              (int(box[0]), int(box[1])),
            #              (int(box[2]), int(box[3])),
            #              (255, 0, 0), 5)

            xtl = int(box[0])
            ytl = int(box[1])
            xbr = int(box[2])
            ybr = int(box[3])
            print(f"xtl {xtl}, ytl {ytl}, xbr {xbr},ybr {ybr}")
            cropped_image = img[ytl: ybr, xtl: xbr]

            retval_result, buffer_result = cv2.imencode('.jpg', cropped_image)
            encoded_cropped_image = base64.b64encode(buffer_result).decode("UTF-8")
            cropped_images_list.append(encoded_cropped_image)

    ##width = int(img.shape[1] * scale_percent / 100)
    ##height = int(img.shape[0] * scale_percent / 100)
    ##dim = (width, height)
    ##img = cv2.resize(img, dim)

    return cropped_images_list, detected


@carddetection.get("/")
def read_root():
    return {"test"}
