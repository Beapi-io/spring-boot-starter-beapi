package io.beapi.api.domain.service

import io.beapi.api.domain.AuthenticationToken
import io.beapi.api.domain.User
import java.util.stream.Stream
import java.util.Date;
import java.util.List;

public interface IAuthenticationToken {
    AuthenticationToken findByToken(String token);
    AuthenticationToken findByUser(User user);
    List<AuthenticationToken> findByExpiryDateLessThan(Long now);
    void deleteByExpiryDateLessThan(Long now);
    void deleteAllExpiredSince(Long now);
}
