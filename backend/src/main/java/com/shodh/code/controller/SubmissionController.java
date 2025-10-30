package com.shodh.code.controller;

import com.shodh.code.dto.SubmissionCreatedDto;
import com.shodh.code.dto.SubmissionRequestDto;
import com.shodh.code.dto.SubmissionStatusDto;
import com.shodh.code.model.Problem;
import com.shodh.code.model.Submission;
import com.shodh.code.repository.ProblemRepository;
import com.shodh.code.service.CodeExecutionService;
import com.shodh.code.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {
    private final SubmissionService submissionService;
    private final ProblemRepository problemRepository;
    private final CodeExecutionService codeExecutionService;

    @PostMapping
    public ResponseEntity<SubmissionCreatedDto> createSubmission(@RequestBody SubmissionRequestDto body) {
        Submission s = new Submission();
        s.setUserId(body.getUserId());
        s.setProblemId(body.getProblemId());
        s.setCode(body.getCode());
        s.setLanguage(body.getLanguage());
        s.setStatus("Pending");
        s.setResult(null);
        s.setScore(null);
        s.setCreatedAt(Instant.now());

        Submission saved = submissionService.create(s);

        // Fire-and-forget execution
        new Thread(() -> {
            try {
                Optional<Problem> p = problemRepository.findById(saved.getProblemId());
                p.ifPresent(problem -> codeExecutionService.runAndEvaluate(saved, problem));
            } catch (Exception ignored) {}
        }).start();

        return ResponseEntity.created(URI.create("/api/submissions/" + saved.getId()))
                .body(new SubmissionCreatedDto(saved.getId()));
    }

    @GetMapping("/{submissionId}")
    public ResponseEntity<SubmissionStatusDto> getSubmission(@PathVariable("submissionId") String submissionId) {
        Optional<Submission> sOpt = submissionService.getById(submissionId);
        if (sOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Submission s = sOpt.get();
        SubmissionStatusDto dto = new SubmissionStatusDto(
                s.getId(),
                s.getStatus(),
                s.getResult(),
                s.getScore()
        );
        return ResponseEntity.ok(dto);
    }
}
