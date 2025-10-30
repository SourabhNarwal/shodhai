package com.shodh.code.service;

import com.shodh.code.model.Contest;
import com.shodh.code.model.LeaderboardEntry;
import com.shodh.code.model.Problem;
import com.shodh.code.model.Submission;
import com.shodh.code.model.User;
import com.shodh.code.repository.ContestRepository;
import com.shodh.code.repository.ProblemRepository;
import com.shodh.code.repository.SubmissionRepository;
import com.shodh.code.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContestService {
    private final ContestRepository contestRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    public Contest create(Contest contest) {
        return contestRepository.save(contest);
    }

    public Optional<Contest> getById(String id) {
        return contestRepository.findById(id);
    }

    public List<Contest> getAll() {
        return contestRepository.findAll();
    }

    public Optional<Contest> update(String id, Contest update) {
        return contestRepository.findById(id).map(existing -> {
            existing.setName(update.getName());
            existing.setStartTime(update.getStartTime());
            existing.setEndTime(update.getEndTime());
            return contestRepository.save(existing);
        });
    }

    public void delete(String id) {
        contestRepository.deleteById(id);
    }

    public List<Problem> getProblems(String contestId) {
        return problemRepository.findByContestId(contestId);
    }

    public List<LeaderboardEntry> getLeaderboard(String contestId) {
        List<Problem> problems = problemRepository.findByContestId(contestId);
        if (problems.isEmpty()) {
            return List.of();
        }
        List<String> problemIds = problems.stream().map(Problem::getId).toList();

        List<Submission> submissions = submissionRepository.findByProblemIdIn(problemIds);
        if (submissions.isEmpty()) {
            return List.of();
        }

        // For each user, sum the best score per problem.
        Map<String, Map<String, Integer>> userProblemBest = new HashMap<>();
        for (Submission s : submissions) {
            String userId = s.getUserId();
            String problemId = s.getProblemId();
            Integer score = s.getScore() != null ? s.getScore() : 0;
            userProblemBest
                .computeIfAbsent(userId, k -> new HashMap<>())
                .merge(problemId, score, Math::max);
        }

        Map<String, Integer> userTotals = new HashMap<>();
        for (Map.Entry<String, Map<String, Integer>> e : userProblemBest.entrySet()) {
            int total = e.getValue().values().stream().mapToInt(Integer::intValue).sum();
            userTotals.put(e.getKey(), total);
        }

        Set<String> userIds = new HashSet<>(userTotals.keySet());
        Map<String, User> usersById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        for (Map.Entry<String, Integer> e : userTotals.entrySet()) {
            User u = usersById.get(e.getKey());
            String username = u != null ? u.getUsername() : "Unknown";
            leaderboard.add(new LeaderboardEntry(e.getKey(), username, e.getValue()));
        }

        leaderboard.sort(Comparator.comparing(LeaderboardEntry::getTotalScore).reversed());
        return leaderboard;
    }
}
