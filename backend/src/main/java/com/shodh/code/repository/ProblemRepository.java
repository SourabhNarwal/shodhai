package com.shodh.code.repository;

import com.shodh.code.model.Problem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProblemRepository extends MongoRepository<Problem, String> {
    List<Problem> findByContestId(String contestId);
}
