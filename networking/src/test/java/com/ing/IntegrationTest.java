package com.ing;

import com.ing.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {
  public static final String MYSQL_IMAGE = "mysql:5.7.39";

  public static Network network = Network.newNetwork();
  @Container
  public static MySQLContainer mySQLContainer =
          (MySQLContainer) new MySQLContainer(DockerImageName.parse(MYSQL_IMAGE))
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

  @Autowired private TestRestTemplate restTemplate;

  @DynamicPropertySource
  public static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", mySQLContainer::getUsername);
    registry.add("spring.datasource.password", mySQLContainer::getPassword);
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
