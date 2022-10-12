package com.ing.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;

@Slf4j
@RestController
public class HealthController {

  private CountDownLatch countDownLatch = new CountDownLatch(10);

  @GetMapping("/health")
  public ResponseEntity<String> health() {
    countDownLatch.countDown();

    log.info("count down {}", countDownLatch.getCount());

    if (countDownLatch.getCount() == 0) {
      log.info("now I'm healthy");

      return ResponseEntity.status(HttpStatus.OK).body("healthy");
    }

    log.info("still unhealthy");

    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("unhealthy");
  }
}
