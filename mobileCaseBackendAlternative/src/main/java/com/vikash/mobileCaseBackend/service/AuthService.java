package com.vikash.mobileCaseBackend.service;

import com.vikash.mobileCaseBackend.model.Admin;
import com.vikash.mobileCaseBackend.model.AuthenticationToken;
import com.vikash.mobileCaseBackend.model.User;
import com.vikash.mobileCaseBackend.repo.IAuthRepo;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
public class AuthService {

    @Autowired
    IAuthRepo iAuthRepo;

    public void createToken(AuthenticationToken token) {

        iAuthRepo.save(token);

    }






    @Transactional
    @Scheduled(fixedRate = 60000)  // runs every 60 seconds
    public void removeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thresholdTime = now.minusMinutes(30);  // 30 minutes ago

        List<AuthenticationToken> expiredTokens = iAuthRepo.findByLastActivityTimeBefore(thresholdTime);
        iAuthRepo.deleteAll(expiredTokens);
    }






    public boolean authenticate(String email, String tokenValue) {
        // Find the AuthenticationToken for the given tokenValue
        AuthenticationToken tokenObj = iAuthRepo.findByTokenValue(tokenValue);

        if (tokenObj != null) {
            User user = tokenObj.getUser();
            Admin admin = tokenObj.getAdmin();

            if (user != null && user.getUserEmail().equals(email)) {
                tokenObj.setLastActivityTime(LocalDateTime.now());
                iAuthRepo.save(tokenObj);
                return true;
            } else if (admin != null && admin.getAdminEmail().equals(email)) {
                // Update the lastActivityTime for the token
                tokenObj.setLastActivityTime(LocalDateTime.now());
                iAuthRepo.save(tokenObj);  // Save the updated tokenObj
                return true;
            }

        }
        return false;
    }

    public void deleteToken(String token) {
        AuthenticationToken authObj = iAuthRepo.findByTokenValue(token);
        iAuthRepo.delete(gitauthObj);
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