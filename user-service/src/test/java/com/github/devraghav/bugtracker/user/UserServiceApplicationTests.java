package com.github.devraghav.bugtracker.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
// @EmbeddedKafka
class UserServiceApplicationTests {

  @Test
  void contextLoads() {}
}
