package com.shodh.code.config;

import com.shodh.code.model.Contest;
import com.shodh.code.model.Problem;
import com.shodh.code.model.User;
import com.shodh.code.repository.ContestRepository;
import com.shodh.code.repository.ProblemRepository;
import com.shodh.code.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {
    private final ContestRepository contestRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    public DataSeeder(ContestRepository contestRepository,
                      ProblemRepository problemRepository,
                      UserRepository userRepository) {
        this.contestRepository = contestRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (contestRepository.count() == 0 && problemRepository.count() == 0) {
            Contest contest = new Contest();
            contest.setName("Intro Contest");
            contest.setStartTime(Instant.now());
            contest.setEndTime(Instant.now().plus(2, ChronoUnit.HOURS));
            contest = contestRepository.save(contest);

            Problem p1 = new Problem();
            p1.setContestId(contest.getId());
            p1.setTitle("Echo");
            p1.setDescription("Read a single line and print it as-is.");
            p1.setInputTestCases(List.of("hello world"));
            p1.setOutputTestCases(List.of("hello world"));

            Problem p2 = new Problem();
            p2.setContestId(contest.getId());
            p2.setTitle("Sum Two Integers");
            p2.setDescription("Read two integers and print their sum.");
            p2.setInputTestCases(List.of("2 3"));
            p2.setOutputTestCases(List.of("5"));

            problemRepository.saveAll(List.of(p1, p2));
        }

        if (userRepository.count() == 0) {
            User u = new User();
            u.setUsername("tester");
            u.setTotalScore(0);
            userRepository.save(u);
        }
    }
}
