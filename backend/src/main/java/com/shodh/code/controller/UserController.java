package com.shodh.code.controller;

import com.shodh.code.model.User;
import com.shodh.code.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PostMapping("/join")
    public ResponseEntity<User> joinContest(@RequestBody User userRequest) {
        if (userRequest.getUsername() == null || userRequest.getUsername().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // Check if user already exists
        Optional<User> existing = userRepository.findByUsername(userRequest.getUsername());
        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get());
        }

        // Create new user
        User newUser = new User();
        newUser.setUsername(userRequest.getUsername());
        newUser.setScore(0);
        User saved = userRepository.save(newUser);

        return ResponseEntity.created(URI.create("/api/users/" + saved.getId())).body(saved);
    }
}
