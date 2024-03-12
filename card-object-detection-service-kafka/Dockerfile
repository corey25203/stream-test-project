FROM python:3.11.7-bookworm

EXPOSE 5003

RUN mkdir /source
WORKDIR /source
ENV APPPATH /source/
ENV KAFKA_CONSUMER_TOPIC source_frames_queue
ENV KAFKA_PRODUCER_TOPIC shape_frames_queue
ENV KAFKA_CONSUMER_GROUP test_group
ENV KAFKA_BOOTSTRAP_SERVERS kafka:9092

#ENV KAFKA_CONSUMER_FETCH_MAX_BYTES 10485880
#ENV KAFKA_CONSUMER_MAX_PARTITION_FETCH_BYTES 10971520
#ENV KAFKA_PRODUCER_MAX_MESSAGE_BYTES 10485880

#RUN git clone https://github.com/opencv/opencv --depth 1
#RUN apt install libopencv-dev python3-opencv

RUN apt-get update -y || true && \
DEBIAN_FRONTEND=noninteractive apt-get install -y tzdata libx11-dev && \
apt-get install -y --no-install-recommends libopencv-dev && \
     rm -rf /var/lib/apt/lists/* && apt autoremove && apt clean

WORKDIR /source

ADD requirements.txt /source/requirements.txt
ADD ocv/fasterrcnn_resnet50_fpn_card_0.pth /source/fasterrcnn_resnet50_fpn_card_0.pth

WORKDIR /source
RUN python -m pip install --upgrade pip
RUN python -m venv /opt/venv

ENV PATH="/opt/venv/bin:$PATH"

RUN . /opt/venv/bin/activate
RUN pip install --no-cache-dir -Ur requirements.txt

WORKDIR /source
COPY ./app /source/app

#CMD ["python", "./detect_card_srv.py"]
CMD ["uvicorn", "app.main:carddetection", "--proxy-headers", "--host", "0.0.0.0", "--port", "5003"]