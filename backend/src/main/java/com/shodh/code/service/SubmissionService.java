package com.shodh.code.service;

import com.shodh.code.model.Submission;
import com.shodh.code.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubmissionService {
    private final SubmissionRepository submissionRepository;

    public Submission create(Submission submission) {
        if (submission.getCreatedAt() == null) {
            submission.setCreatedAt(Instant.now());
        }
        return submissionRepository.save(submission);
    }

    public Optional<Submission> getById(String id) {
        return submissionRepository.findById(id);
    }

    public List<Submission> getByUser(String userId) {
        return submissionRepository.findByUserId(userId);
    }

    public List<Submission> getByProblem(String problemId) {
        return submissionRepository.findByProblemId(problemId);
    }

    public List<Submission> getByProblemIds(List<String> problemIds) {
        return submissionRepository.findByProblemIdIn(problemIds);
    }

    public Optional<Submission> updateStatus(String id, String status, String result, Integer score) {
        return submissionRepository.findById(id).map(existing -> {
            existing.setStatus(status);
            existing.setResult(result);
            if (score != null) {
                existing.setScore(score);
            }
            return submissionRepository.save(existing);
        });
    }
}
