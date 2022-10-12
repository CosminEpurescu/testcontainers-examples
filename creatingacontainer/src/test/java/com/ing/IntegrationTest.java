package com.ing;

import com.ing.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {
  public static final String MYSQL_IMAGE = "mysql:5.7.39";

  @Autowired private TestRestTemplate restTemplate;

  @Container
  public static MySQLContainer mySQLContainer =
          new MySQLContainer(DockerImageName.parse(MYSQL_IMAGE))
                  .withDatabaseName("eis")
                  .withUsername("admin")
                  .withPassword("admin");


  @DynamicPropertySource
  public static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", mySQLContainer::getUsername);
    registry.add("spring.datasource.password", mySQLContainer::getPassword);
  }

  @Test
  public void insertingFirstCustomer() {
    Customer customer = Customer.builder().firstName("Jane").lastName("Dean").build();


    restTemplate.postForEntity("/customer", new HttpEntity<>(customer), Customer.class);
    Customer actual = restTemplate.getForEntity("/customer/1", Customer.class).getBody();

    assertEquals(customer.getFirstName(), actual.getFirstName());
    assertEquals(customer.getLastName(), actual.getLastName());
  }

  @Test
  public void insertingSecondCustomer() {

    Customer customer = Customer.builder().firstName("Chris").lastName("Prat").build();

    restTemplate.postForEntity("/customer", new HttpEntity<>(customer), Customer.class);
    Customer actual = restTemplate.getForEntity("/customer/2", Customer.class).getBody();

    assertEquals(customer.getFirstName(), actual.getFirstName());
    assertEquals(customer.getLastName(), actual.getLastName());
  }
}
