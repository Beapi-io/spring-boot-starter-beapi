package io.beapi.api.repositories

import io.beapi.api.domain.AuthenticationToken;
import io.beapi.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Date;
import java.util.stream.Stream;

public interface AuthenticationTokenRepository extends JpaRepository<AuthenticationToken, Long> {

    AuthenticationToken findByToken(String token);
    AuthenticationToken findByUser(User user);
    Stream<AuthenticationToken> findByExpiryDateLessThan(Long now);
    void deleteByExpiryDateLessThan(Long now);

    @Modifying
    @Query("delete from AuthenticationToken t where t.expiryDate <= ?1")
    void deleteAllExpiredSince(Long now);

}