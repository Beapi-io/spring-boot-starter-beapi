package io.beapi.api.repositories;

import io.beapi.api.domain.Authority;
import io.beapi.api.domain.User;
import io.beapi.api.domain.UserAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
//import org.hibernate.validator.constraints.NotEmpty;

@Repository
public interface UserAuthorityRepository extends JpaRepository<UserAuthority, Long> {
    public UserAuthority save(UserAuthority userAuthority);

    @Query("SELECT u FROM UserAuthority u WHERE u.user = ?1")
    List<UserAuthority> findByUser(User user);

    @Query("SELECT u FROM UserAuthority u WHERE u.authority = ?1")
    List<UserAuthority> findByAuthority(Authority authority);

}