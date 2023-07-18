package io.beapi.api.repositories;

import io.beapi.api.domain.Authority;
import io.beapi.api.domain.User;
import io.beapi.api.domain.UserAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

//import org.hibernate.validator.constraints.NotEmpty;

@Repository
public interface UserAuthorityRepository extends JpaRepository<UserAuthority, Long> {

    public UserAuthority save(UserAuthority userAuthority);
    List<UserAuthority> findByUser(User user);
    List<UserAuthority> findByAuthority(Authority authority);

}