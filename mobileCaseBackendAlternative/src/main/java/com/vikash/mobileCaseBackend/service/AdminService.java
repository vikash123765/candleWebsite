package com.vikash.mobileCaseBackend.service;


import com.vikash.mobileCaseBackend.model.Admin;
import com.vikash.mobileCaseBackend.model.AuthenticationToken;
import com.vikash.mobileCaseBackend.model.Product;
import com.vikash.mobileCaseBackend.repo.IAuthRepo;
import com.vikash.mobileCaseBackend.repo.IRepoAdmin;
import com.vikash.mobileCaseBackend.service.EmailUtility.MailHandlerBase;
import com.vikash.mobileCaseBackend.service.HashingUtility.PasswordEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class AdminService {


    @Autowired
    AuthService authService;
    @Autowired
    IRepoAdmin repoAdmin;

    @Autowired
    IAuthRepo authRepo;

    @Autowired
    ProductService productService;

    public ResponseEntity<String> adminSgnOut(String token) {


        if (authService.authenticateSignOut(token)) {
            authService.deleteToken(token);
            return  new ResponseEntity<>( "sign out successfull", HttpStatus.OK);

        } else {
            return new ResponseEntity<>("un authorized access",HttpStatus.UNAUTHORIZED);
        }


    }

    public ResponseEntity<String> adminSignIn(String email, String password) {

        // check if admiin exists via the email

        Admin existingAdmin = repoAdmin.findByAdminEmail(email);
        if (existingAdmin == null) {
            return new ResponseEntity<>("not valid email,please sign up first!",HttpStatus.BAD_REQUEST);

        }
        try {
            String encryptedPass = PasswordEncryptor.encrypt(password);
            if (existingAdmin.getAdminPassword().equals(encryptedPass)) {
                // login should be allowed using token
                AuthenticationToken token = new AuthenticationToken(existingAdmin);


                authService.createToken(token);

                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Token", "token=" + token.getTokenValue());
                headers.add("Access-Control-Expose-Headers", "X-Token");
                headers.add("Access-Control-Allow-Headers", "X-Token");
                headers.add("Access-Control-Expose-Headers","*-Token");

                if (MailHandlerBase.sendEmail(email, "otp after login", token.getTokenValue())) {

                    return new ResponseEntity<>( "check email for otp/token ", headers, HttpStatus.OK);



                } else {
                    return new ResponseEntity<>("error while generating token",HttpStatus.INTERNAL_SERVER_ERROR);
                }

            }
            else {
                //password was wrong!!!
                return new ResponseEntity<>( "Invalid Credentials!!!",HttpStatus.UNAUTHORIZED);
            }
        } catch (NoSuchAlgorithmException e) {
            return  new ResponseEntity<>("internal server issue while saving password,try again!",HttpStatus.INTERNAL_SERVER_ERROR);


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


   public List<Product> getProductsById(List<Integer> ids) {
        return productService.getProductsById(ids);
    }

    public ResponseEntity<Boolean> isAdminLoggedIn(String adminToken) {

        AuthenticationToken tokenObj  = authRepo.findByTokenValue(adminToken);


        if(tokenObj== null || tokenObj.getTokenValue() == null  ){
            return new ResponseEntity<>( false,HttpStatus.NOT_FOUND);
        }else {
            return new ResponseEntity<>(  true,HttpStatus.OK);
        }

    }
 /*
    public ResponseEntity<Boolean> adminLoggedInOrNot(String adminEmail) {
        AuthenticationToken tokenObj  = authRepo.findTokenValueByAdmin(adminEmail);
        if(tokenObj.getTokenValue() == null){
            return new ResponseEntity<>(false,HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(true,HttpStatus.OK);
        }
    }*/
}

