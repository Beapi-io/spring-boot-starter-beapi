package io.beapi.api.repositories;

import io.beapi.api.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {

    public List<Authority> findAll();
    public Authority save(Authority auth);
    public Authority findByAuthority(String authority);
    public void deleteById(Long id);


}