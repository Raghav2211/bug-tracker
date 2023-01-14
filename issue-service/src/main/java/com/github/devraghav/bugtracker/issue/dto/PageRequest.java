package com.github.devraghav.bugtracker.issue.dto;

import lombok.Getter;
import org.springframework.data.domain.Sort;
import org.springframework.web.reactive.function.server.ServerRequest;

@Getter
public class PageRequest {
  private Integer page;
  private Integer size;
  private Sort sort;

  private PageRequest(Integer page, Integer size, Sort sort) {
    this.page = page;
    this.size = size;
    this.sort = sort;
  }

  public static PageRequest of(ServerRequest request) {
    return new PageRequest(
        request.queryParam("page").map(Integer::parseInt).orElseGet(() -> 0),
        request.queryParam("size").map(Integer::parseInt).orElseGet(() -> 10),
        request.queryParam("sort").map(Sort::by).orElseGet(() -> Sort.by("createdAt")));
  }
}
