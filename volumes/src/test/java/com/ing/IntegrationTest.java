package com.ing;

import com.ing.model.Customer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DirtiesContext
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
              .withNetworkAliases("db")
              .withClasspathResourceMapping("/db_setup", "/db_setup", BindMode.READ_WRITE);

  @Container
  public static GenericContainer phpMyAdmin =
      new GenericContainer(DockerImageName.parse("phpmyadmin:5.2.0"))
          .dependsOn(mySQLContainer)
          .withEnv("PMA_HOST", "db")
          .withEnv("MYSQL_ROOT_PASSWORD", "admin")
          .withNetwork(network)
          .withExposedPorts(80);

  @Autowired private TestRestTemplate restTemplate;

  @DynamicPropertySource
  public static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", mySQLContainer::getUsername);
    registry.add("spring.datasource.password", mySQLContainer::getPassword);
  }

  @BeforeEach
  public void beforeEach() throws IOException, InterruptedException {
    // init the database
    mySQLContainer.execInContainer("sh", "db_setup/init_db.sh");
  }

  @AfterEach
  public void afterEach() throws IOException, InterruptedException {
    // clear the database
    mySQLContainer.execInContainer("sh", "db_setup/clear_db.sh");
  }

  @Test
  public void test() {
    System.out.println("PhpMyAdmin port: " + phpMyAdmin.getFirstMappedPort());

    Customer customer = Customer.builder().firstName("Jane").lastName("Dean").build();

    restTemplate.postForEntity("/customer", new HttpEntity<>(customer), Customer.class);
    Customer actual = restTemplate.getForEntity("/customer/1", Customer.class).getBody();

    assertEquals(customer.getFirstName(), actual.getFirstName());
    assertEquals(customer.getLastName(), actual.getLastName());
  }
}
