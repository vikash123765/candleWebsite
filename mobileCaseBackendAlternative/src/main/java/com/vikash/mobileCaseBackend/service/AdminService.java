package com.vikash.mobileCaseBackend.service;


import com.vikash.mobileCaseBackend.model.Admin;
import com.vikash.mobileCaseBackend.model.AuthenticationToken;
import com.vikash.mobileCaseBackend.model.Product;
import com.vikash.mobileCaseBackend.model.User;
import com.vikash.mobileCaseBackend.repo.IAuthRepo;
import com.vikash.mobileCaseBackend.repo.IRepoAdmin;
import com.vikash.mobileCaseBackend.repo.IRepoProduct;
import com.vikash.mobileCaseBackend.service.EmailUtility.MailHandlerBase;
import com.vikash.mobileCaseBackend.service.HashingUtility.PasswordEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
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
    IRepoProduct iRepoProduct;



    public ResponseEntity<String> adminSgnOut(String token) {


        if (authService.authenticateSignOut(token)) {
            authService.deleteToken(token);
            return  new ResponseEntity<>( "sign out successfull", HttpStatus.OK);

        } else {
            return new ResponseEntity<>("un authorized access",HttpStatus.UNAUTHORIZED);
        }


    }

    public ResponseEntity<String> adminSignIn(String email, String password) {
        Admin existingAdmin = repoAdmin.findByAdminEmail(email);

        if (existingAdmin != null) {
            try {
                String encryptedPass = PasswordEncryptor.encrypt(password);

                if (existingAdmin.getAdminPassword().equals(encryptedPass)) {
                    if (existingAdmin.getAuthenticationToken() != null) {
                        // Token exists, update its creation time
                        AuthenticationToken tokenObj = existingAdmin.getAuthenticationToken();
                        LocalDateTime tokenCreationDateTime = LocalDateTime.now();
                        tokenObj.setTokenCreationDateTime(tokenCreationDateTime);
                        authService.saveToken(tokenObj);

                        HttpHeaders headers = new HttpHeaders();
                        headers.add("X-Token", "token=" + tokenObj.getTokenValue());
                        headers.add("Access-Control-Expose-Headers", "X-Token");
                        headers.add("Access-Control-Allow-Headers", "X-Token");

                        return new ResponseEntity<>("Login successful!", headers, HttpStatus.OK);
                    } else {
                        // Token doesn't exist, create a new one
                        AuthenticationToken token = new AuthenticationToken(existingAdmin);
                        authService.createToken(token);

                        HttpHeaders headers = new HttpHeaders();
                        headers.add("X-Token", "token=" + token.getTokenValue());
                        headers.add("Access-Control-Expose-Headers", "X-Token");
                        headers.add("Access-Control-Allow-Headers", "X-Token");
                        headers.add("Access-Control-Expose-Headers", "*-Token");

                        if (MailHandlerBase.sendEmail(email, "otp after login", token.getTokenValue())) {
                            return new ResponseEntity<>("Check email for OTP/token", headers, HttpStatus.OK);
                        } else {
                            return new ResponseEntity<>("Error while generating token", HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                    }
                } else {
                    // Password was wrong
                    return new ResponseEntity<>("Invalid Credentials!!!", HttpStatus.UNAUTHORIZED);
                }
            } catch (NoSuchAlgorithmException e) {
                return new ResponseEntity<>("Internal server issue while saving password, try again!", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            // Admin with the given email doesn't exist
            return new ResponseEntity<>("Admin not found!", HttpStatus.NOT_FOUND);
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




    public ResponseEntity<Boolean> isAdminLoggedIn(String adminToken) {

        AuthenticationToken tokenObj  = authRepo.findByTokenValue(adminToken);


        if(tokenObj== null || tokenObj.getTokenValue() == null  ){
            return new ResponseEntity<>( false,HttpStatus.NOT_FOUND);
        }else {
            return new ResponseEntity<>(  true,HttpStatus.OK);
        }

    }

  /*  public ResponseEntity<String> getAdminToken(String adminEmail) {
        AuthenticationToken token = authRepo.findByAdmin_AdminEmail(adminEmail);
        if (token != null) {
            return new ResponseEntity<>("this is the " + token.getTokenValue() + " value for : " + adminEmail, HttpStatus.OK);
        }
        return new ResponseEntity<>("there is no token value for this email", HttpStatus.NOT_FOUND);
    }*/


 /*
    public ResponseEntity<Boolean> adminLoggedInOrNot(String adminEmail) {
        AuthenticationToken tokenObj  = authRepo.findTokenValueByAdmin(adminEmail);
        if(tokenObj.getTokenValue() == null){
            return new ResponseEntity<>(false,HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(true,HttpStatus.OK);
        }
    }*/

     void markProductUnavailable(Integer productId) {
        Product product = iRepoProduct.findById(productId).orElse(null);
        if (product != null) {
            product.setProductAvailable(false);
            iRepoProduct.save(product);
            System.out.println("Product with ID " + productId + " marked as unavailable.");
        }
    }
}

