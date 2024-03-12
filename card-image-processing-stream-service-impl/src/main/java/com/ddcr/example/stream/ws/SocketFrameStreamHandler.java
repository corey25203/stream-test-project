package com.ddcr.example.stream.ws;

import com.ddcr.example.stream.dao.model.SourceFrame;
import com.ddcr.example.stream.kafka.SourceFrameKafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SocketFrameStreamHandler implements WebSocketHandler {

    @Autowired
    SourceFrameKafkaConsumer sourceFrameKafkaConsumer;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Mono<Void> input = session.receive().then();
        Flux<SourceFrame> frames = sourceFrameKafkaConsumer.consumeAppUpdates();
        Mono<Void> output = session
                .send(frames.map(sourceFrame -> sourceFrame.getData())
                        .map(session::textMessage));
        return Mono.zip(input, output).then();
    }

}
