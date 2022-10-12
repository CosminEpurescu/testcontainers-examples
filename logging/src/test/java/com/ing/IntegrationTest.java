package com.ing;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.output.WaitingConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static io.restassured.RestAssured.*;

@Slf4j
@Testcontainers
public class IntegrationTest {
  public static final String MYSQL_IMAGE = "mysql:5.7.39";

  public static Network network = Network.newNetwork();

  @Container
  public static MySQLContainer mySQLContainer =
      (MySQLContainer)
          new MySQLContainer(DockerImageName.parse(MYSQL_IMAGE))
              .withDatabaseName("dbname")
              .withUsername("admin")
              .withPassword("admin")
              .withNetwork(network)
              .withNetworkAliases("db");

  @Container
  public static GenericContainer phpMyAdmin =
      new GenericContainer(DockerImageName.parse("phpmyadmin:5.2.0"))
          .dependsOn(mySQLContainer)
          .withEnv("PMA_HOST", "db")
          .withEnv("MYSQL_ROOT_PASSWORD", "admin")
          .withNetwork(network)
          .withExposedPorts(80);

  @Container
  public static GenericContainer apiContainer =
      new GenericContainer(
              new ImageFromDockerfile()
                  // path -> build context path
                  // resourcePath -> the path to the resource based on the classpath
                  .withFileFromClasspath("/api.jar", "/api.jar")
                  .withDockerfileFromBuilder(
                      builder ->
                          builder
                              .from("adoptopenjdk/openjdk11:ubi")
                              .copy("/api.jar", "app.jar")
                              .entryPoint("java", "-jar", "/app.jar")
                              .build()))
          .dependsOn(mySQLContainer)
          .withNetwork(network)
          .withEnv("spring.datasource.url", "jdbc:mysql://db:3306/dbname")
          .withEnv("spring.datasource.username", "admin")
          .withEnv("spring.datasource.password", "admin")
          .withExposedPorts(8080);

  @BeforeEach
  public void beforeEach() {
    baseURI = "http://localhost";
    port = apiContainer.getFirstMappedPort();
  }

  @AfterEach
  public void afterEach() {
    reset();
  }


  @Test
  public void getAllLogs() {
    final String logs = apiContainer.getLogs();

    log.info("these are the logs {}{}", System.lineSeparator(), logs);
  }

  @Test
  public void getStdOutLogs() {
    final String stdoutLogs = apiContainer.getLogs(OutputFrame.OutputType.STDOUT);
    final String stderrLogs = apiContainer.getLogs(OutputFrame.OutputType.STDERR);
    final String endLogs = apiContainer.getLogs(OutputFrame.OutputType.END);

    log.info("these are stdout logs {}{}", System.lineSeparator(), stdoutLogs);
    log.info("these are stderr logs {}{}", System.lineSeparator(), stderrLogs);
    log.info("these are end logs {}{}", System.lineSeparator(), endLogs);
  }

  @Test
  public void streamingLogs() {
    final Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(log).withSeparateOutputStreams();

    apiContainer.followOutput(logConsumer);
  }

  @Test
  public void otherConsumers() throws TimeoutException {
    WaitingConsumer waitingConsumer = new WaitingConsumer();
    ToStringConsumer toStringConsumer = new ToStringConsumer();

    Consumer<OutputFrame> composedConsumer = toStringConsumer.andThen(waitingConsumer);
    apiContainer.followOutput(composedConsumer);

    get("/log");

    waitingConsumer.waitUntil(frame ->
            frame.getUtf8String().contains("task executed"), 30, TimeUnit.SECONDS, 10);

    log.info("my wait ended");
  }
}
