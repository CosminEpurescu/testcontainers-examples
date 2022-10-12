package com.ing;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static io.restassured.RestAssured.*;

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
                  // resource path -> the path to the docker file directory relative to the
                  // classpath
                  // it picks up automatically everything in the directory where it finds a docker
                  // file
                  .withFileFromClasspath(".", "."))
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
  public void withFile() {
    given()
        .contentType(ContentType.JSON)
        .with()
        .body(
            "{\n"
                + "  \"id\": 1,\n"
                + "  \"firstName\": \"Andrei\",\n"
                + "  \"lastName\": \"Epurescu\"\n"
                + "}")
        .post("/customer");

    given().get("/customer/1").body().prettyPrint();
  }
}
