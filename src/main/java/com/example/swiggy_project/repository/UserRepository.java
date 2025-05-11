package com.example.swiggy_project.repository;

import com.example.swiggy_project.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}