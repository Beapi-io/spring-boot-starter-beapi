package io.beapi.api.domain.service;



import io.beapi.api.domain.Authority;
import io.beapi.api.repositories.AuthorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
//@Qualifier
public class AuthorityService implements IAuthority {

    AuthorityRepository authrepo;

    @Autowired
    public AuthorityService(AuthorityRepository authrepo) {
        this.authrepo = authrepo;
    }

    @Override
    public List<Authority> findAll() {
        return authrepo.findAll();
    }

    @Override
    public Authority save(Authority authority) {
        authrepo.saveAndFlush(authority);
        return authority;
    }

    public Authority findByAuthority(String authority){
        return authrepo.findByAuthority(authority);
    }

    public Optional<Authority> findById(Long id) {
        return authrepo.findById(id);
    }

    @Override
    public Optional<Authority> findById(int id) {
        return authrepo.findById(Long.valueOf(id));
    }


    public void deleteById(Long id) {
        authrepo.deleteById(id);
        authrepo.flush();
    }

}
