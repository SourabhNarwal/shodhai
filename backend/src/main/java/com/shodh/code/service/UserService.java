package com.shodh.code.service;

import com.shodh.code.model.User;
import com.shodh.code.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User create(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getById(String id) {
        return userRepository.findById(id);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public Optional<User> update(String id, User update) {
        return userRepository.findById(id).map(existing -> {
            existing.setUsername(update.getUsername());
            existing.setTotalScore(update.getTotalScore());
            return userRepository.save(existing);
        });
    }

    public void delete(String id) {
        userRepository.deleteById(id);
    }

    public User getOrCreateByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername(username);
                    u.setTotalScore(0);
                    return userRepository.save(u);
                });
    }

    public Optional<User> addScore(String userId, int delta) {
        return userRepository.findById(userId).map(u -> {
            int current = u.getTotalScore() != null ? u.getTotalScore() : 0;
            u.setTotalScore(current + delta);
            return userRepository.save(u);
        });
    }
}
