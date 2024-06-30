package io.beapi.api.domain.service;

import io.beapi.api.domain.User;
import io.beapi.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture;

@Service
public class UserService implements IUser {

    @Autowired
    UserRepository userrepo;

    @Autowired
    public UserService(UserRepository userrepo) {
        this.userrepo = userrepo;
    }

    @Override
    public List<User> getAllUsers() { return userrepo.findAll(); }

    //@Override
    public User findById(Long id) {
        return userrepo.findById(id);
    }

    @Override
    public Optional<User> findById(int id) {
        return userrepo.findById(Long.valueOf(id));
    }

    @Override
    public User findByEmail(String email) {
        return userrepo.findByEmail(email);
    }



    @Override
    public User findByUsername(String username) {
        return userrepo.findByUsername(username);
    }

    @Override
    public User save(User usr) {
        try{
            userrepo.save(usr);
            userrepo.flush();
            return usr;
        }catch (DataAccessException e){
            throw new Exception(e.getCause().getCause().getLocalizedMessage())
        }
    }

    //@Override
    public void deleteById(Long id) {
        userrepo.deleteById(id);
        userrepo.flush();
    }

    @Override
    public void deleteById(int id) {
        userrepo.deleteById(Long.valueOf(id));
        userrepo.flush();
    }

    @Override
    public User bootstrapUser(User usr) {
        try{
            userrepo.save(usr);
            userrepo.flush();
            return usr;
        }catch (DataAccessException e){
            throw new Exception(e.getCause().getCause().getLocalizedMessage())
        }
    }

}
