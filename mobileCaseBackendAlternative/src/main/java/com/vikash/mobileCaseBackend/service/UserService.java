package com.vikash.mobileCaseBackend.service;


import com.vikash.mobileCaseBackend.model.AuthenticationToken;
import com.vikash.mobileCaseBackend.model.User;
import com.vikash.mobileCaseBackend.model.UserInfoDTO;
import com.vikash.mobileCaseBackend.model.enums.Gender;
import com.vikash.mobileCaseBackend.repo.IAuthRepo;
import com.vikash.mobileCaseBackend.repo.IRepoUser;

import com.vikash.mobileCaseBackend.service.EmailUtility.MailHandlerBase;
import com.vikash.mobileCaseBackend.service.HashingUtility.PasswordEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@Service
public class UserService {

    @Autowired
    IRepoUser userRepo;

    @Autowired
    AuthService authService;

    @Autowired
    IAuthRepo iAuthRepo;


    public ResponseEntity<String> userSignUp(User newUser) {
        // Check if user already exists
        String newEmail = newUser.getUserEmail();
        User ifExistUser = userRepo.findByUserEmail(newEmail);
        if (ifExistUser != null) {
            return new ResponseEntity<>("email already exists please enter unused one!", HttpStatus.BAD_REQUEST);
        }

        String currentPassword = newUser.getUserPassword();
        try {
            String encryptedPass = PasswordEncryptor.encrypt(currentPassword);
            newUser.setUserPassword(encryptedPass);
            userRepo.save(newUser);
            return new ResponseEntity<>("account created!", HttpStatus.CREATED); // Using 201 Created status

        } catch (NoSuchAlgorithmException e) {
            return new ResponseEntity<>("internal server issue while saving password, try again!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public ResponseEntity<String> userSignIn(String email, String password) {
        User existingUser = userRepo.findByUserEmail(email);
        if (existingUser == null) {
            return new ResponseEntity<>("not valid email, please sign up first!", HttpStatus.BAD_REQUEST);
        }

        try {
            String encryptedPassword = PasswordEncryptor.encrypt(password);

            if (existingUser.getUserPassword().equals(encryptedPassword)) {
                AuthenticationToken token = new AuthenticationToken(existingUser);
                if (MailHandlerBase.sendEmail(email, "otp after login", token.getTokenValue())) {
                    authService.createToken(token);

                    // Create a cookie header with the token value
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("X-Token", "token=" + token.getTokenValue());
                    headers.add("Access-Control-Expose-Headers", "X-Token");
                    headers.add("Access-Control-Allow-Headers", "X-Token");

                    return new ResponseEntity<>("Login successful! Check email for otp/token!!!", headers, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("error while generating token!!!", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>("Invalid Credentials!!!", HttpStatus.UNAUTHORIZED);
            }
        } catch (NoSuchAlgorithmException e) {
            return new ResponseEntity<>("Internal Server issues while saving password, try again later!!!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public ResponseEntity<String> userSgnOut(String token) {


        if (authService.authenticateSignOut(token)) {
            authService.deleteToken(token);
            return new ResponseEntity<>("sign out successfull", HttpStatus.OK);

        } else {
            return new ResponseEntity<>("un authorized access", HttpStatus.UNAUTHORIZED);
        }


    }


    public ResponseEntity<?> userSingedInInfo(String token) {
        AuthenticationToken actualToken = iAuthRepo.findByTokenValue(token);

        if (actualToken == null) {
            return new ResponseEntity<>("User is not logged in", HttpStatus.NOT_FOUND);
        }

        if (actualToken.getUser() != null) {
            UserInfoDTO userInfoDTO = new UserInfoDTO();
            User user = actualToken.getUser();
            userInfoDTO.setUserName(user.getUserName());
            userInfoDTO.setUserEmail(user.getUserEmail());
            userInfoDTO.setPhoneNumber(user.getPhoneNumber());
            userInfoDTO.setAddress(user.getAddress());
            // Note: It's not recommended to include sensitive data like passwords in DTOs
            userInfoDTO.setPassword(user.getUserPassword());
            userInfoDTO.setGender(user.getGender()); // Replace 'Gender' with the actual type

            return ResponseEntity.ok(userInfoDTO);
        } else {
            return new ResponseEntity<>("User is not logged in", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<String> alterUserInfo(String token, User user) throws NoSuchAlgorithmException {

        AuthenticationToken actualToken = iAuthRepo.findByTokenValue(token);
        User userBefore = actualToken.getUser();

        if (actualToken.getUser() != null) {
            userBefore.setUserName(user.getUserName());
            userBefore.setUserEmail(user.getUserEmail());
            userBefore.setAddress(user.getAddress());
            userBefore.setPhoneNumber(user.getPhoneNumber());
            userBefore.setAddress(user.getAddress());
            userBefore.setGender(user.getGender());
            String currentPassword = userBefore.getUserPassword();
            String encryptedPass = PasswordEncryptor.encrypt(currentPassword);
            userBefore.setUserPassword(encryptedPass);
            userRepo.save(userBefore);
            return new ResponseEntity<>("User information altered successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("smething went wrong when altering the user information or user does not exist ", HttpStatus.NOT_FOUND);
        }
    }

    public String changePassword(String token, String oldPassword, String newPassword) throws NoSuchAlgorithmException {
        AuthenticationToken actualToken = iAuthRepo.findByTokenValue(token);

        if (actualToken != null) {
            User user = actualToken.getUser();
            String currentPassword = actualToken.getUser().getUserPassword();

            String encryptOldPassword = PasswordEncryptor.encrypt(oldPassword);


            if (currentPassword.equals(encryptOldPassword)) {
                String encryptedPass = PasswordEncryptor.encrypt(newPassword);
                user.setUserPassword(encryptedPass);
                userRepo.save(user);
                return "User password changed successfully!";
            } else {
                return "Old password doesn't match the current password.";
            }
        } else {
            return "Invalid authentication token.";
        }
    }
}



