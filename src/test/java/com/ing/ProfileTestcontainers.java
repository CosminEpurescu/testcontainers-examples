package com.ing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

@Testcontainers
public class ProfileTestcontainers {

  public GenericContainer profileContainer;
  public static Map<String, String> sysctls =
      Map.of(
          "kernel.msgmnb", "6553600",
          "kernel.msgmax", "1048800",
          "kernel.msgmni", "32768",
          "kernel.sem", "128 32768 128 4096");

  @BeforeEach
  public void setUp() {
    profileContainer =
        new GenericContainer(
                DockerImageName.parse(
                    "p01373dockerglobal.azurecr.io/profile-core/7.6.4/host:latest"))
            .withEnv("user", "profile:fis")
            .withExposedPorts(10666, 2022);

    profileContainer.start();
  }

  @AfterEach
  public void tearDown() {
    if (profileContainer != null) {
      profileContainer.stop();
      profileContainer = null;
    }
  }

  @Test
  public void one() {
    System.out.println("here");
  }
}
