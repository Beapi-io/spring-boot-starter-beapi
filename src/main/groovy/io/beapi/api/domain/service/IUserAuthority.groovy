package io.beapi.api.domain.service;

import io.beapi.api.domain.Authority;
import io.beapi.api.domain.User;
import io.beapi.api.domain.UserAuthority;

import java.util.List;

public interface IUserAuthority {

    UserAuthority save(UserAuthority userAuthority);
    List<UserAuthority> findByUser(User user);
    List<UserAuthority> findByAuthority(Authority authority);
}
