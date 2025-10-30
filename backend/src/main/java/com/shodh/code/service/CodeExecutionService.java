package com.shodh.code.service;

import com.shodh.code.model.Problem;
import com.shodh.code.model.Submission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CodeExecutionService {
    private static final Duration PROCESS_TIMEOUT = Duration.ofSeconds(20);
    private static final String JAVA_IMAGE = "openjdk:17";
    private static final String PY_IMAGE = "python:3.11";
    private static final int DEFAULT_MAX_SCORE = 100;

    private final SubmissionService submissionService;
    private final UserService userService;

    public void runAndEvaluate(Submission submission, Problem problem) {
        String submissionId = submission.getId();
        submissionService.updateStatus(submissionId, "Running", null, null);

        Path tempDir = null;
        try {
            tempDir = Paths.get("backend/tmp-exec/" + sanitize(submissionId));
            Files.createDirectories(tempDir);

            // prepare test cases
            List<String> inputs = problem.getInputTestCases();
            List<String> expectedOutputs = problem.getOutputTestCases();
            int testCount = Math.max(1, Math.min(inputs == null ? 0 : inputs.size(),
                                              expectedOutputs == null ? 0 : expectedOutputs.size()));
            if (testCount == 0) {
                submissionService.updateStatus(submissionId, "Error", "No testcases", 0);
                return;
            }

            // detect language
            String language = (submission.getLanguage() == null) ? "java" : submission.getLanguage().toLowerCase(Locale.ROOT);

            // Write user code
            if ("python".equals(language)) {
                Path sol = tempDir.resolve("solution.py");
                Files.writeString(sol, submission.getCode() == null ? "" : submission.getCode(), StandardCharsets.UTF_8);
            } else { // default java
                Path sol = tempDir.resolve("Solution.java");
                Files.writeString(sol, submission.getCode() == null ? "" : submission.getCode(), StandardCharsets.UTF_8);
            }

            // write all input files
            for (int i = 0; i < testCount; ++i) {
                Path in = tempDir.resolve("input" + i + ".txt");
                String inStr = (inputs != null && i < inputs.size()) ? inputs.get(i) : "";
                Files.writeString(in, inStr == null ? "" : inStr, StandardCharsets.UTF_8);
            }

            // Prepare container image and base commands
            boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
            String mountPath = resolveDockerMountPath(tempDir);

            // If Java -> compile once
            if ("java".equals(language)) {
                String compileCmd = buildDockerCommand(mountPath, JAVA_IMAGE,
                        "sh -c \"cd /app && javac Solution.java\"");
                ExecResult compileRes = spawn(compileCmd, isWindows);
                System.out.println("[judge] compile exit=" + compileRes.code);
                if (compileRes.output != null && !compileRes.output.isEmpty()) System.out.println("[judge] compile output:\n" + compileRes.output);
                if (compileRes.code != 0) {
                    submissionService.updateStatus(submissionId, "Error", "Compilation Error", 0);
                    return;
                }
            }

            // run each test
            int passed = 0;
            StringBuilder runOutputLog = new StringBuilder();
            for (int i = 0; i < testCount; ++i) {
                String runCmd;
                if ("python".equals(language)) {
                    // python: run solution.py
                    runCmd = buildDockerCommand(mountPath, PY_IMAGE,
                            "sh -c \"cd /app && timeout 5 python3 solution.py < input" + i + ".txt > output" + i + ".txt\"");
                } else {
                    // java: run compiled class
                    runCmd = buildDockerCommand(mountPath, JAVA_IMAGE,
                            "sh -c \"cd /app && timeout 5 java Solution < input" + i + ".txt > output" + i + ".txt\"");
                }

                ExecResult runRes = spawn(runCmd, isWindows);
                runOutputLog.append("[judge] test#").append(i).append(" exit=").append(runRes.code).append("\n");
                if (runRes.output != null && !runRes.output.isEmpty()) runOutputLog.append(runRes.output).append("\n");

                // if exit code non-zero, treat as runtime error for that test
                if (runRes.code != 0) {
                    // keep trying other tests, but this one fails
                    continue;
                }

                Path outPath = tempDir.resolve("output" + i + ".txt");
                String actual = Files.exists(outPath) ? Files.readString(outPath, StandardCharsets.UTF_8) : "";
                String expected = (expectedOutputs != null && i < expectedOutputs.size()) ? expectedOutputs.get(i) : "";

                boolean ok = normalize(actual).equals(normalize(expected));
                if (ok) passed++;
            }

            // compute score (equal weight)
            int maxScore = (problem.getMaxScore() != null) ? problem.getMaxScore() : DEFAULT_MAX_SCORE;
            int score = (int) Math.round(((double) passed / testCount) * maxScore);

            if (passed == testCount) {
                submissionService.updateStatus(submissionId, "Accepted", "All test cases passed", score);
                if (submission.getUserId() != null) userService.addScore(submission.getUserId(), score);
            } else if (passed == 0) {
                // If none passed, check if any run produced non-zero exit => runtime/compilation errors
                // We already captured compile errors earlier; if runs exited non-zero, classify as Error
                // To be conservative, if any run had non-zero exit -> Error, else Wrong Answer.
                boolean anyNonZero = runOutputLog.toString().contains("exit=-1") || runOutputLog.toString().contains("exit=1") || runOutputLog.toString().contains("exit=2");
                if (anyNonZero) {
                    submissionService.updateStatus(submissionId, "Error", "Compilation/Runtime Error", 0);
                } else {
                    submissionService.updateStatus(submissionId, "Wrong Answer", "No tests passed", 0);
                }
            } else {
                submissionService.updateStatus(submissionId, "Partially Accepted", passed + "/" + testCount + " passed", score);
                if (submission.getUserId() != null) userService.addScore(submission.getUserId(), score);
            }

        } catch (Exception e) {
            submissionService.updateStatus(submissionId, "Error", "System Error", 0);
            e.printStackTrace();
        } finally {
            if (tempDir != null) {
                try { deleteRecursively(tempDir); } catch (IOException ignored) {}
            }
        }
    }

    private static String buildDockerCommand(String mountPath, String image, String innerCommand) {
        // mountPath already normalized
        return "docker run --rm -v " + mountPath + ":/app " + image + " " + innerCommand;
    }

    private static String firstOrEmpty(List<String> list) {
        return (list != null && !list.isEmpty()) ? list.get(0) : "";
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().replace("\r\n", "\n").replace("\r", "\n");
    }

    private static String sanitize(String s) {
        if (s == null) return "x";
        return s.replaceAll("[^a-zA-Z0-9-_]", "_");
    }

    private static String resolveDockerMountPath(Path path) {
        String abs = path.toAbsolutePath().toString();
        // Windows -> convert C:\path -> /c/path for Docker, but Docker Desktop often accepts C:/... as well.
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win")) {
            abs = abs.replace("\\", "/");
            // If starts with "C:/", convert to "/c/..."
            if (abs.length() > 1 && abs.charAt(1) == ':') {
                char drive = Character.toLowerCase(abs.charAt(0));
                abs = "/" + drive + abs.substring(2);
            }
        }
        return abs;
    }

    private ExecResult spawn(String fullCommand, boolean isWindows) throws IOException, InterruptedException {
        ProcessBuilder pb = isWindows
                ? new ProcessBuilder("cmd", "/c", fullCommand)
                : new ProcessBuilder("sh", "-c", fullCommand);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String output = readStream(p.getInputStream());
        boolean finished = p.waitFor(PROCESS_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        if (!finished) {
            p.destroyForcibly();
            return new ExecResult(-1, output);
        }
        int code = p.exitValue();
        return new ExecResult(code, output);
    }

    private static String readStream(InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }
    
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    

    private static class ExecResult {
        final int code;
        final String output;
        ExecResult(int code, String output) { this.code = code; this.output = output; }
    }
}
