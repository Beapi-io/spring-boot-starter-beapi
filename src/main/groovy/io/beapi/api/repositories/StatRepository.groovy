package io.beapi.api.repositories

import io.beapi.api.domain.Stat
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
public interface StatRepository extends JpaRepository<Stat, Long> {
    public List<Stat> findAll()
    public Stat findById(Long id);
}
