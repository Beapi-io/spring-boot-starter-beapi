package io.beapi.api.domain.service;

import io.beapi.api.domain.Authority;
import io.beapi.api.domain.User;
import io.beapi.api.domain.UserAuthority;
import io.beapi.api.repositories.UserAuthorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAuthorityService{

    UserAuthorityRepository userauthrepo;

    @Autowired
    public UserAuthorityService(UserAuthorityRepository userauthrepo) {
        this.userauthrepo = userauthrepo;
    }

    //@Override
    public List<UserAuthority> findByUser(User user){
        return userauthrepo.findByUser(user);
    }

    //@Override
    public List<UserAuthority> findByAuthority(Authority auth){
        return userauthrepo.findByAuthority(auth);
    }

    //@Override
    public UserAuthority save(UserAuthority userAuthority){
        userauthrepo.save(userAuthority);
        userauthrepo.flush();
        return userAuthority;
    }

    //@Override
    public void deleteById(Long id) {
        userauthrepo.deleteById(id);
        userauthrepo.flush();
    }

}
