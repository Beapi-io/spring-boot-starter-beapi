package io.beapi.api.repositories;

import io.beapi.api.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {

    @Query("SELECT a FROM Authority a")
    public List<Authority> findAll();
    public Authority save(Authority auth);

    @Query("SELECT a FROM Authority a WHERE a.authority = ?1")
    public Authority findByAuthority(String authority);
    public void deleteById(Long id);


}