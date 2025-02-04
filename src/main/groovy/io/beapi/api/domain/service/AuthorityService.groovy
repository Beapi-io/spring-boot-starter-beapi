package io.beapi.api.domain.service;



import io.beapi.api.domain.Authority;
import io.beapi.api.repositories.AuthorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.hibernate.SessionFactory
import org.hibernate.Session
import org.hibernate.Transaction
import org.springframework.dao.DataAccessException;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional

@Service
//@Qualifier
public class AuthorityService {

    @Autowired
    private SessionFactory sessionFactory;

    AuthorityRepository authrepo;

    @Autowired
    public AuthorityService(AuthorityRepository authrepo) {
        this.authrepo = authrepo;
    }

    //@Override
    public List<Authority> findAll() {
        return authrepo.findAll();
    }

    //@Override
    @Transactional(rollbackFor = Exception.class)
    public Authority save(Authority authority) {
        try{
            authrepo.saveAndFlush(authority);
            return authority;
        }catch (DataAccessException e){
            throw new Exception("{AuhtorityService :: save] ",e)
            throw new Exception(e.getCause().getCause().getLocalizedMessage())
        }
    }

    public Authority findByAuthority(String authority){
        return authrepo.findByAuthority(authority);
    }

    public Optional<Authority> findById(Long id) {
        return authrepo.findById(id);
    }

    //@Override
    public Optional<Authority> findById(int id) {
        return authrepo.findById(Long.valueOf(id));
    }


    public void deleteById(Long id) {
        authrepo.deleteById(id);
        authrepo.flush();
    }

}
