package com.shodh.code.controller;

import com.shodh.code.model.Problem;
import com.shodh.code.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    // Create a new problem (POST /api/problems)
    @PostMapping
    public ResponseEntity<Problem> createProblem(@RequestBody Problem problem) {
        Problem saved = problemService.create(problem);
        return ResponseEntity.created(URI.create("/api/problems/" + saved.getId())).body(saved);
    }

    // Get all problems
    @GetMapping
    public ResponseEntity<List<Problem>> getAllProblems() {
        List<Problem> problems = problemService.getAll();
        return ResponseEntity.ok(problems);
    }

    // Get a specific problem by ID
    @GetMapping("/{id}")
    public ResponseEntity<Problem> getProblemById(@PathVariable("id") String id) {
        Optional<Problem> problemOpt = problemService.getById(id);
        return problemOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
