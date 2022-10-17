package com.ing;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static io.restassured.RestAssured.*;

@Slf4j
@Testcontainers
public class IntegrationTest {
  public static final String MYSQL_IMAGE = "mysql:5.7.39";

  public static Network network = Network.newNetwork();

  private static final WaitStrategy httpHealthCheckStrategy =
      Wait.forHttp("/health")
          .forStatusCode(200)
          .forResponsePredicate(response -> response.equals("healthy"));

  private static final WaitStrategy logWaitStrategy = Wait.forLogMessage(".*Started.*", 1);

  private static final WaitStrategy dockerHealthCheckWaitStrategy = Wait.forHealthcheck();

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
          .withExposedPorts(8080)
          .waitingFor(httpHealthCheckStrategy);

  @BeforeEach
  public void beforeEach() {
    baseURI = "http://localhost";
    port = apiContainer.getFirstMappedPort();
  }

  @AfterEach
  public void afterEach() {
    RestAssured.reset();
  }

  @Test
  public void test() {
    log.info("test executed");
  }
}
