package io.beapi.api.service

import io.beapi.api.domain.User
import io.beapi.api.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.context.ApplicationContext

@Service
public class DomainObjectService{

    public DomainObjectService() {

    }

    public void getDomainObject(String className){
        // List<String>
        def tmp = className.split("Controller")
        println("test : "+tmp[0]+"Service")
        println(tmp)

        try{
            def bean = context.getBean(tmp[0].toLowerCase()+"Service");
        }catch(Exception e){
            println("[DomainObjectService > getDomainObject] : Exception - full stack trace follows :", e)
        }

    }

}
