package io.beapi.api.repositories;

import io.beapi.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    public List<User> findAll()
    public User findByEmail(String email);
    public User findByUsername(String username);
    public User findById(Long id);
    public User findByVerificationCode(String id);
}