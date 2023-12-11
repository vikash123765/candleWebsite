package com.vikash.mobileCaseBackend.service;


import com.vikash.mobileCaseBackend.model.Admin;
import com.vikash.mobileCaseBackend.model.AuthenticationToken;
import com.vikash.mobileCaseBackend.repo.IRepoAdmin;
import com.vikash.mobileCaseBackend.service.EmailUtility.MailHandlerBase;
import com.vikash.mobileCaseBackend.service.HashingUtility.PasswordEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;

@Service
public class AdminService {

    @Autowired
    AuthService authService;
    @Autowired
    IRepoAdmin repoAdmin;



    public String adminSgnOut(String email, String token) {


        if (authService.authenticate(email, token)) {
            authService.deleteToken(token);
            return "sign out successfull";

        } else {
            return "un authorized access";
        }


    }

    public String adminSignIn(String email, String password) {

        // check if admiin exists via the email

        Admin existingAdmin = repoAdmin.findByAdminEmail(email);
        if (existingAdmin == null) {
            return "not valid email,please sign up first!";

        }
        try {
            String encryptedPass = PasswordEncryptor.encrypt(password);
            if (existingAdmin.getAdminPassword().equals(encryptedPass)) {
                // login should be allowed using token
                AuthenticationToken token = new AuthenticationToken(existingAdmin);
                if (MailHandlerBase.sendEmail(email, "otp after login", token.getTokenValue())) {
                    authService.createToken(token);
                    return "check email for otp/token ";


                } else {
                    return "error while generating token";
                }

            }
            else {
                //password was wrong!!!
                return "Invalid Credentials!!!";
            }
        } catch (NoSuchAlgorithmException e) {
            return "internal server issue while saving password,try again!";


        }

    }


    public String adminSignUp(Admin newAdmin) {

            //check if user already exists
            String newEmail = newAdmin.getAdminEmail();
            Admin ifExistAdmin = repoAdmin.findByAdminEmail(newEmail);
            if (ifExistAdmin != null) {
                return "email already exists please enter unused one!";

            }
            String currentPassword = newAdmin.getAdminPassword();
            try {
                String encryptedPass = PasswordEncryptor.encrypt(currentPassword);
                newAdmin.setAdminPassword(encryptedPass);
                repoAdmin.save(newAdmin);
                return "account created!";

            } catch (NoSuchAlgorithmException e) {
                return "internal server issue while saving password,try again!";

            }




        }
    }

