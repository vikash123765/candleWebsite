package com.vikash.mobileCaseBackend.service;

import com.vikash.mobileCaseBackend.model.Admin;
import com.vikash.mobileCaseBackend.model.AuthenticationToken;
import com.vikash.mobileCaseBackend.model.User;
import com.vikash.mobileCaseBackend.repo.IAuthRepo;

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

        if (tokenObj != null) {
            User user = tokenObj.getUser();
            Admin admin = tokenObj.getAdmin();


            if (user != null && user.getUserEmail().equals(email)) {
                return true;
            } else if (admin != null && admin.getAdminEmail().equals(email)) {
                return true;
            }

        }
        return false;
    }

    public void deleteToken(String token) {
        AuthenticationToken authObj = iAuthRepo.findByTokenValue(token);
        iAuthRepo.delete(authObj);
    }

    public boolean authenticateSignOut(String token) {
        // Find the AuthenticationToken for the given tokenValue

        AuthenticationToken tokenObj = iAuthRepo.findByTokenValue(token);
        if (tokenObj != null) {
            String tokenOfLoggedInUser = tokenObj.getTokenValue();

            if (tokenOfLoggedInUser != null && tokenOfLoggedInUser.equals(token)) {
                return true;
            } else {

                return false;

            }

        }else {
            return false;
        }


    }


    public boolean authenticateUserLoggedIn(String token) {
        AuthenticationToken tokenObj = iAuthRepo.findByTokenValue(token);
        if (tokenObj != null) {
            String tokenOfLoggedInUser = tokenObj.getTokenValue();

            if (tokenOfLoggedInUser != null && tokenOfLoggedInUser.equals(token) ) {
                return true;
            } else {

                return false;

            }

        }else {
            return false;
        }

    }
}