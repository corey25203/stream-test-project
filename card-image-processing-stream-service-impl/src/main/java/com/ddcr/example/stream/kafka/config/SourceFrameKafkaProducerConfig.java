package com.ddcr.example.stream.kafka.config;


import com.ddcr.example.stream.dao.model.SourceFrame;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.SenderOptions;


@Configuration
public class SourceFrameKafkaProducerConfig {

    @Bean
    public SenderOptions<String, SourceFrame> producerProps(KafkaProperties kafkaProperties) {
        //TODO:
        return SenderOptions.create(kafkaProperties.buildProducerProperties());
    }

    @Bean
    public ReactiveKafkaProducerTemplate<String, SourceFrame> reactiveKafkaProducerTemplate(
            SenderOptions<String, SourceFrame> producerProps) {
        return new ReactiveKafkaProducerTemplate<>(producerProps);
    }
}
