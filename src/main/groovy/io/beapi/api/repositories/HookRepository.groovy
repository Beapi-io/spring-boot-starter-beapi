package io.beapi.api.repositories;


import io.beapi.api.domain.Hook;
import io.beapi.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//import org.hibernate.validator.constraints.NotEmpty;

@Repository
public interface HookRepository extends JpaRepository<Hook, Long> {

    public Hook save(Hook hook);
    public void deleteById(Long id);
    List<Hook> findByUser(User user);
    Optional<Hook> findById(Long id);
    Hook findByServiceAndUser(String service, User user);
    List<Hook> findByEnabledTrueAndService(String service);
}