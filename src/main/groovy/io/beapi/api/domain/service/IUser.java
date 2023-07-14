package io.beapi.api.domain.service;

import io.beapi.api.domain.User;

import java.util.List;
import java.util.Optional;

public interface IUser {
    List<User> getAllUsers();
    Optional<User> findById(int id);
    User findByEmail(String email);
    User findByUsername(String username);
    User save(User usr);
    void deleteById(int id);
    User bootstrapUser(User usr);
}
