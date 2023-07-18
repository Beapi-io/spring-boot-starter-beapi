package io.beapi.api.domain.service;

import io.beapi.api.domain.Authority;

import java.util.List;
import java.util.Optional;

public interface IAuthority {

    List<Authority> findAll();

    Authority save(Authority Role);

    void deleteById(Long id);

    Optional<Authority> findById(int id);

}
