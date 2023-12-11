package com.vikash.mobileCaseBackend.service;


import com.vikash.mobileCaseBackend.model.AuthenticationToken;
import com.vikash.mobileCaseBackend.model.User;
import com.vikash.mobileCaseBackend.repo.IRepoUser;

import com.vikash.mobileCaseBackend.service.EmailUtility.MailHandlerBase;
import com.vikash.mobileCaseBackend.service.HashingUtility.PasswordEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;

@Service
public class UserService {

    @Autowired
    IRepoUser userRepo;

    @Autowired
    AuthService authService;



    public String userSignUp(User newUser) {
        //check if user already exists
        String newEmail = newUser.getUserEmail();
        User ifExistUser = userRepo.findByUserEmail(newEmail);
        if (ifExistUser != null) {
            return "email already exists please enter unused one!";

        }
        String currentPassword = newUser.getUserPassword();
        try {
            String encryptedPass = PasswordEncryptor.encrypt(currentPassword);
            newUser.setUserPassword(encryptedPass);
            userRepo.save(newUser);
            return "account created!";

        } catch (NoSuchAlgorithmException e) {
            return "internal server issue while saving password,try again!";

        }


    }




    public String UserSignIn(String email, String password) {

        // check if user exists via the email

        User existingUser = userRepo.findByUserEmail(email);
        if (existingUser == null) {
            return "not valid email,please sign up first!";

        }
        try {
            String encryptedPassword = PasswordEncryptor.encrypt(password);

            if(existingUser.getUserPassword().equals(encryptedPassword))
            {
                //login should be allowed using token
                AuthenticationToken token  = new AuthenticationToken(existingUser);

                if(MailHandlerBase.sendEmail(email,"otp after login", token.getTokenValue())) {
                    authService.createToken(token);
                    return "check email for otp/token!!!";
                }
                else {
                    return "error while generating token!!!";
                }
            }
            else {
                //password was wrong!!!
                return "Invalid Credentials!!!";
            }
        } catch (NoSuchAlgorithmException e) {
            return "Internal Server issues while saving password, try again later!!!";
        }
    }


    public String userSgnOut(String email, String token) {


        if (authService.authenticate(email, token)) {
            authService.deleteToken(token);
            return "sign out successfull";

        } else {
            return "un authorized access";
        }


    }


}



