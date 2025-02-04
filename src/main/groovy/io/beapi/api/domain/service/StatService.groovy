package io.beapi.api.domain.service

import io.beapi.api.domain.Stat
import io.beapi.api.domain.User
import io.beapi.api.repositories.StatRepository
import io.beapi.api.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service

@Service
public class StatService {

	@Autowired StatRepository repo;

	@Autowired
	public StatService(StatRepository repo) {
		this.repo = repo;
	}

	//@Override
	public Stat findById(Long id) {
		return repo.findById(id);
	}

	//@Override
	public Optional<Stat> findById(int id) {
		return repo.findById(Long.valueOf(id));
	}

	//@Override
	public List<Stat> findAll() {
		return repo.findAll(Sort.Order(Sort.Direction.ASC, "statusCode"));
	}

	//@Override
	public Stat save() {
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

	//@Override
	public void deleteById(int id) {
		userrepo.deleteById(Long.valueOf(id));
		userrepo.flush();
	}


}
