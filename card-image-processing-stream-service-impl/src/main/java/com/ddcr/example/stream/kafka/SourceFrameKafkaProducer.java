package com.ddcr.example.stream.kafka;


import com.ddcr.example.stream.dao.model.SourceFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SourceFrameKafkaProducer {

    @Value(value = "${PRODUCER_TOPIC}")
    private String topic;
    @Autowired
    private ReactiveKafkaProducerTemplate<String, SourceFrame> reactiveKafkaProducerTemplate;

    public void sendMessages(SourceFrame sourceFrame) {
        log.info("send to topic={}, {}= id{},", topic, SourceFrame.class.getSimpleName(), String.valueOf(sourceFrame.getId()));
        reactiveKafkaProducerTemplate.send(topic, String.valueOf(sourceFrame.getId()), sourceFrame)
                .doOnSuccess(senderResult -> log.info("sent {} offset : {}",
                        "data",
                        senderResult.recordMetadata().offset()))
                .subscribe();
    }

}
