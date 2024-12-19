package io.beapi.api.service;

import io.beapi.api.domain.AuthenticationToken
import io.beapi.api.repositories.AuthenticationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//import jakarta.transaction.Transactional;
import java.util.Calendar;

@Service
//@Transactional
public class UserSecurityService {

    @Autowired
    private AuthenticationTokenRepository authTokenRepo;


    public String validatePasswordResetToken(String token) {
        final AuthenticationToken passToken = authTokenRepo.findByToken(token);

        return !isTokenFound(passToken) ? "invalidToken"
                : isTokenExpired(passToken) ? "expired"
                : null;
    }

    private boolean isTokenFound(AuthenticationToken authToken) {
        return authToken != null;
    }

    private boolean isTokenExpired(AuthenticationToken authToken) {
        final Calendar cal = Calendar.getInstance();
        return authToken.getExpiryDate().before(cal.getTime());
    }
}
