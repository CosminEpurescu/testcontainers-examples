package com.ing.kafka;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@Slf4j
@RequiredArgsConstructor
@Getter
public class KafkaConsumer {
    private CountDownLatch latch = new CountDownLatch(1);
    private String payload;

    @KafkaListener(topics = "${test.topic}")
    public void receive(ConsumerRecord<String, String> consumerRecord) {
        log.info("received payload='{}'", consumerRecord.toString());

        payload = consumerRecord.toString();

        latch.countDown();
    }

    public void resetLatch() {
        latch = new CountDownLatch(1);
    }
}
