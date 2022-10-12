package com.ing.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Timer;
import java.util.TimerTask;

@Slf4j
@RestController
public class LogController {

  @GetMapping("/log")
  public void log() {
    TimerTask timerTask =
        new TimerTask() {
          @Override
          public void run() {
            log.info("task executed");
          }
        };

    Timer timer = new Timer();

    timer.scheduleAtFixedRate(timerTask, 5000, 2000);
  }
}
