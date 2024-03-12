from typing import Union
import base64
import json
# import aiokafka
import json
import torchvision
import numpy as np
import os
import cv2
import easyocr

# from torchvision.ops.boxes import nms
from fastapi import FastAPI

from confluent_kafka import Consumer, Producer, KafkaException, KafkaError
import socket

running = True
reader = easyocr.Reader(['en', 'en'])
# from confluent_kafka.schema_registry import SchemaRegistryClient
# from confluent_kafka.serialization import StringSerializer
# from confluent_kafka.schema_registry.avro import AvroSerializer
# from confluent_kafka import SerializingProducer

KAFKA_CONSUMER_TOPIC = os.getenv('KAFKA_CONSUMER_TOPIC', 'shape_frames_queue')
KAFKA_PRODUCER_TOPIC = os.getenv('KAFKA_PRODUCER_TOPIC', 'detections_frames_queue')
KAFKA_CONSUMER_GROUP = os.getenv('KAFKA_CONSUMER_GROUP', 'test_group')
KAFKA_BOOTSTRAP_SERVERS = os.getenv('KAFKA_BOOTSTRAP_SERVERS', 'kafka:9092')
KAFKA_CONSUMER_FETCH_MAX_BYTES = os.getenv('KAFKA_CONSUMER_FETCH_MAX_BYTES', 10485880)
KAFKA_CONSUMER_MAX_PARTITION_FETCH_BYTES = os.getenv('KAFKA_CONSUMER_MAX_PARTITION_FETCH_BYTES', 10971520)
KAFKA_PRODUCER_MAX_MESSAGE_BYTES = os.getenv('KAFKA_PRODUCER_MAX_MESSAGE_BYTES', 10485880)

consumer_conf = {
    'bootstrap.servers': KAFKA_BOOTSTRAP_SERVERS,
    'group.id': KAFKA_CONSUMER_GROUP,
    'enable.auto.commit': 'false',
    'auto.offset.reset': 'earliest',
    'max.partition.fetch.bytes': KAFKA_CONSUMER_MAX_PARTITION_FETCH_BYTES,
    'fetch.max.bytes': KAFKA_CONSUMER_FETCH_MAX_BYTES
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

textdetection = FastAPI()


def shutdown():
    print('shutdown actions')


@textdetection.on_event("startup")
async def startup_event():
    # await aioproducer.start()
    print('log info startup 3')
    basic_consume_loop(consumer, consumer_topics)


@textdetection.on_event("shutdown")
async def shutdown_event():
    # await aioproducer.stop()
    shutdown()
    print('log info shutdown')


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

                text_img, detected = recognize_text_data(encodedImg, 90)

                print(f"create result : {detected} ")
                SourceFrame = {
                    "id": encodedImgId,
                    "streamId": "",
                    "sessionId": "",
                    "data": text_img,
                    "description": "",
                    "status": "",
                    "dataContentInfo": ""
                }

                json_data = json.dumps(SourceFrame)
                producer.produce(producer_topic, key="key", value=json_data, callback=acked_poll)
                ##for cr_img in cropped_images_list:
                ##    SourceFrame = {"data": cr_img, "id": encodedImgId}

                ##    json_data = json.dumps(SourceFrame)
                ##    producer.produce(producer_topic, key="key", value=json_data, callback=acked_poll)
                producer.poll()

    finally:
        consumer.close()


def recognize_text_data(origb64encoded, iou_threshold=0.1, threshold=0.8, scale_percent=70):
    orig = base64.b64decode(origb64encoded)
    img = cv2.imdecode(np.frombuffer(orig, dtype=np.int8), flags=1)



    results = reader.readtext(img, decoder='greedy', beamWidth=5, batch_size=1, \
                              workers=0, allowlist='0123456789', blocklist=None, detail=1, \
                              rotation_info=None, paragraph=False, min_size=20, \
                              contrast_ths=0.1, adjust_contrast=0.5, filter_ths=0.003, \
                              text_threshold=0.7, low_text=0.4, link_threshold=0.4, \
                              canvas_size=2560, mag_ratio=1., \
                              slope_ths=0.1, ycenter_ths=0.5, height_ths=0.5, \
                              width_ths=0.99, y_ths=0.7, x_ths=3.0, add_margin=0.1, output_format='standard')

    lab = cv2.cvtColor(img, cv2.COLOR_BGR2LAB)

    r, th = cv2.threshold(lab[:, :, 1], 125, 255, cv2.THRESH_BINARY_INV)

    text_img = img.copy()
    detected = 'test'
    for (bbox, text, prob) in results[:5]:
        (tl, tr, br, bl) = bbox
        tl = (int(tl[0]), int(tl[1]))
        tr = (int(tr[0]), int(tr[1]))
        br = (int(br[0]), int(br[1]))
        bl = (int(bl[0]), int(bl[1]))
        text_img = cv2.rectangle(text_img, tl, br, (0, 0, 255), 3)
        text_img = cv2.putText(text_img, text, (tl[0], tl[1] - 20), cv2.FONT_HERSHEY_SIMPLEX, 1.1, (0, 0, 0), 5)

    retval_result, buffer_result = cv2.imencode('.jpg', text_img)
    encoded_text_img = base64.b64encode(buffer_result).decode("UTF-8")

    return encoded_text_img, detected;


@textdetection.get("/")
def read_root():
    return {"test 3"}
