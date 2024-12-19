package io.beapi.api.domain.service

import io.beapi.api.domain.User;
import io.beapi.api.domain.AuthenticationToken
import io.beapi.api.repositories.AuthenticationTokenRepository
import io.beapi.api.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import java.util.List;

@Service
public class AuthenticationTokenService implements IAuthenticationToken {

    @Autowired AuthenticationTokenRepository authTokenRepo;

    //@Autowired
    //public AuthenticationTokenService(AuthenticationTokenRepository authTokenRepo) {
    //    this.authTokenRepo = authTokenRepo;
    //}

    @Override
    AuthenticationToken findByToken(String token){
        return authTokenRepo.findByToken(token);
    }

    @Override
    AuthenticationToken findByUser(User user){
        return authTokenRepo.findByUser(user);
    }

    @Override
    List<AuthenticationToken> findByExpiryDateLessThan(Long now){
        List<AuthenticationToken> tokens = authTokenRepo.findByExpiryDateLessThan(now);
        return tokens
    }


    void deleteByExpiryDateLessThan(Long now){
        authTokenRepo.deleteByExpiryDateLessThan(now);
    }

    void deleteAllExpiredSince(Long now){
        authTokenRepo.deleteAllExpiredSince(now);
    }

    public AuthenticationToken save(AuthenticationToken authToken) {
        try{
            authTokenRepo.save(authToken);
            authTokenRepo.flush();
            return authToken;
        }catch (Exception e){
            throw new Exception("[AuthenticationTokenService :: save] : Exception", e);
        }
    }

    public void createAuthenticationToken(User user, String token) {
        AuthenticationToken myToken = new AuthenticationToken(token, user);
        authTokenRepo.save(myToken);
    }
}
