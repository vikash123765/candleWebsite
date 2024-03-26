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
import java.util.stream.Collectors;

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
        LocalDateTime thresholdTime = now.minusMinutes(30); // chnage to 30 after you ty it out

        List<AuthenticationToken> allTokens = iAuthRepo.findAll();
        List<AuthenticationToken> tokensToDelete = allTokens.stream()
                .filter(token -> token.getTokenCreationDateTime().isBefore(thresholdTime))
                .collect(Collectors.toList());

        iAuthRepo.deleteAll(tokensToDelete);
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
    public void saveToken(AuthenticationToken tokenObj) {

        iAuthRepo.save(tokenObj);
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