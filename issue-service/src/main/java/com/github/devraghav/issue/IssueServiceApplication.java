package com.github.devraghav.issue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.github.devraghav"})
public class IssueServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(IssueServiceApplication.class, args);
  }
}
