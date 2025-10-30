package com.shodh.code.repository;

import com.shodh.code.model.Submission;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SubmissionRepository extends MongoRepository<Submission, String> {
    List<Submission> findByUserId(String userId);
    List<Submission> findByProblemId(String problemId);
    List<Submission> findByProblemIdIn(List<String> problemIds);
    List<Submission> findByProblemIdAndUserId(String problemId, String userId);
}
