package com.github.devraghav.user.repository;

import com.github.devraghav.user.entity.UserEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ReactiveMongoRepository<UserEntity, String> {}
