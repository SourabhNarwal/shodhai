package com.shodh.code.service;

import com.shodh.code.model.Problem;
import com.shodh.code.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;

    public Problem create(Problem problem) {
        return problemRepository.save(problem);
    }

    public List<Problem> getAll() {
        return problemRepository.findAll();
    }

    public Optional<Problem> getById(String id) {
        return problemRepository.findById(id);
    }
}
