package com.vikash.instBackend.service;

import com.vikash.instBackend.model.Admin;
import com.vikash.instBackend.model.AuthenticationToken;
import com.vikash.instBackend.model.User;
import com.vikash.instBackend.repo.IAuthRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    IAuthRepo iAuthRepo;

    public void createToken(AuthenticationToken token) {
        iAuthRepo.save(token);

    }


    public boolean authenticate(String email, String tokenValue) {
        // Find the AuthenticationToken for the given tokenValue
        AuthenticationToken tokenObj = iAuthRepo.findByTokenValue(tokenValue);

        if(tokenObj != null){
            User user =tokenObj.getUser();
            Admin admin= tokenObj.getAdmin();


        if(user != null &&  user.getUserEmail().equals(email)){
            return true;
        }else if(admin != null && admin.getAdminEmail().equals(email)){
            return true;
        }

    }
        return false;
    }

    public void deleteToken(String token) {
        AuthenticationToken authObj = iAuthRepo.findByTokenValue(token);
        iAuthRepo.delete(authObj);
    }
}
