package com.github.devraghav.bugtracker.issue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"app.edge-service.url=http://localhost:8000"})
class IssueCommandServiceApplicationTests {

  @Test
  void contextLoads() {}
}
