package com.shodh.code.controller;

import com.shodh.code.dto.ContestDetailsDto;
import com.shodh.code.dto.LeaderboardEntryDto;
import com.shodh.code.dto.ProblemDto;
import com.shodh.code.model.Contest;
import com.shodh.code.model.LeaderboardEntry;
import com.shodh.code.model.Problem;
import com.shodh.code.service.ContestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/contests")
@RequiredArgsConstructor
public class ContestController {
    private final ContestService contestService;

    @PostMapping
    public ResponseEntity<Contest> createContest(@RequestBody Contest contest) {
        Contest created = contestService.create(contest);
        return ResponseEntity.ok(created);
    }
    
    @GetMapping
    public ResponseEntity<List<Contest>> getAllContests() {
        List<Contest> contests = contestService.getAll();
        return ResponseEntity.ok(contests);
    }

    @GetMapping("/{contestId}")
    public ResponseEntity<ContestDetailsDto> getContest(@PathVariable("contestId") String contestId) {
        Optional<Contest> contestOpt = contestService.getById(contestId);
        if (contestOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Contest contest = contestOpt.get();
        List<Problem> problems = contestService.getProblems(contestId);
        List<ProblemDto> problemDtos = problems.stream()
                .map(p -> new ProblemDto(p.getId(), p.getTitle(), p.getDescription()))
                .toList();
        ContestDetailsDto dto = new ContestDetailsDto(
                contest.getId(),
                contest.getName(),
                contest.getStartTime(),
                contest.getEndTime(),
                problemDtos
        );
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{contestId}/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDto>> getLeaderboard(@PathVariable("contestId") String contestId) {
        List<LeaderboardEntry> entries = contestService.getLeaderboard(contestId);
        List<LeaderboardEntryDto> dtos = entries.stream()
                .map(e -> new LeaderboardEntryDto(e.getUserId(), e.getUsername(), e.getTotalScore()))
                .toList();
        return ResponseEntity.ok(dtos);
    }
}
