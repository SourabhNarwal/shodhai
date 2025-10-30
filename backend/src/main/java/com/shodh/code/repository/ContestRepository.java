package com.shodh.code.repository;

import com.shodh.code.model.Contest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContestRepository extends MongoRepository<Contest, String> {
}
