package com.ddcr.example.stream.kafka.config;

import com.ddcr.example.stream.dao.model.SourceFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;

@Configuration
@Slf4j
public class SourceFrameKafkaConsumerConfig {

    @Bean
    public ReceiverOptions<String, SourceFrame> kafkaReceiverOptions(@Value(value = "${CONSUMER_TOPIC}") String topic,
                                                                     KafkaProperties kafkaProperties) {
        //TODO:
        ReceiverOptions<String, SourceFrame> basicReceiverOptions =
                ReceiverOptions.create(kafkaProperties.buildConsumerProperties());
        return basicReceiverOptions.subscription(Collections.singletonList(topic))
                .addAssignListener(partitions -> log.info("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.info("onPartitionsRevoked {}", partitions));
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, SourceFrame> reactiveKafkaConsumerTemplate(
            ReceiverOptions<String, SourceFrame> kafkaReceiverOptions) {
        return new ReactiveKafkaConsumerTemplate<>(kafkaReceiverOptions);
    }
}
