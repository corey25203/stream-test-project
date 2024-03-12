package com.ddcr.example.stream.kafka;


import com.ddcr.example.stream.dao.model.SourceFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class SourceFrameKafkaConsumer {

    @Autowired
    private ReactiveKafkaConsumerTemplate<String, SourceFrame> reactiveKafkaConsumerTemplate;

    public Flux<SourceFrame> consumeAppUpdates() {
        return reactiveKafkaConsumerTemplate
                .receiveAutoAck()
                .map(ConsumerRecord::value)
                .doOnNext(imageProcessingRequest -> {
                    log.info("successfully consumed {}= id {}", SourceFrame.class.getSimpleName(), imageProcessingRequest.getId());
                })
                .doOnError(throwable -> log.error("something went wrong while consuming : {}", throwable.getMessage()));
    }

}
