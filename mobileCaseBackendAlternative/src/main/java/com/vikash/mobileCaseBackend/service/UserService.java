package com.vikash.mobileCaseBackend.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vikash.mobileCaseBackend.model.AuthenticationToken;
import com.vikash.mobileCaseBackend.model.User;
import com.vikash.mobileCaseBackend.model.UserInfoDTO;
import com.vikash.mobileCaseBackend.repo.IAuthRepo;
import com.vikash.mobileCaseBackend.repo.IRepoUser;

import com.vikash.mobileCaseBackend.service.EmailUtility.MailHandlerBase;
import com.vikash.mobileCaseBackend.service.HashingUtility.PasswordEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

import javax.mail.MessagingException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    IRepoUser userRepo;

    @Autowired
    AuthService authService;

    @Autowired
    IAuthRepo iAuthRepo;
    /*  @Autowired
      MailHandlerBase mailHandlerBase;

  */
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResponseEntity<Map<String, String>> userSignUp(User newUser) throws JsonProcessingException {
        // Check if user already exists
        String newEmail = newUser.getUserEmail();
        User ifExistUser = userRepo.findByUserEmail(newEmail);
        if (ifExistUser != null) {
            if (ifExistUser.getUserEmail() != null && ifExistUser.getUserPassword() != null) {
                Map<String, String> responseBody = Map.of("message", "registered_user");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(responseBody);
            }

            if (ifExistUser.getUserEmail() != null) {
                Map<String, String> responseBody = Map.of("message", "guest_user");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(responseBody);
            }
        }
        String currentPassword = newUser.getUserPassword();
        try {
            String encryptedPass = PasswordEncryptor.encrypt(currentPassword);
            newUser.setUserPassword(encryptedPass);
            userRepo.save(newUser);
            String email = newUser.getUserEmail();
            MailHandlerBase.sendEmail(email, "user account created!", "congratulations you are have registered onVTS cases!!");
            Map<String, String> responseBody = Map.of("message", "account_created");
            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody); // Using 201 Created status

        } catch (NoSuchAlgorithmException e) {
            Map<String, String> responseBody = Map.of("message", "internal server issue while saving password, try again!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);

        }
    }


    public ResponseEntity<String> userSignIn(String email, String password) {
        User existingUser = userRepo.findByUserEmail(email);

        if (existingUser == null) {
            return new ResponseEntity<>("Not a valid email, please sign up first!", HttpStatus.BAD_REQUEST);
        }

        try {
            String encryptedPassword = PasswordEncryptor.encrypt(password);

            if (existingUser.getUserPassword().equals(encryptedPassword)) {
                AuthenticationToken token = new AuthenticationToken(existingUser);

                // if (MailHandlerBase.sendEmail(email, "user signed in", "congratulations")) {
                authService.createToken(token);

                // Create a cookie header with the token value
                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Token", "token=" + token.getTokenValue());
                headers.add("Access-Control-Expose-Headers", "X-Token");
                headers.add("Access-Control-Allow-Headers", "X-Token");

                return new ResponseEntity<>("Login successful!", headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Error while generating token!!!", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Invalid Credentials!!!", HttpStatus.UNAUTHORIZED);
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

            long phoneNumber = user.getPhoneNumber();

            if (phoneNumber >= Integer.MIN_VALUE && phoneNumber <= Integer.MAX_VALUE) {
                userInfoDTO.setPhoneNumber((Long) phoneNumber);
            } else {
                // Handle the case where the phone number is too large for an int
                // For example, throw an exception or set a default value
                userInfoDTO.setPhoneNumber(0L);  // Set a default value or throw an exception
            }

            userInfoDTO.setAddress(user.getAddress());
            userInfoDTO.setPassword(user.getUserPassword());
            userInfoDTO.setGender(user.getGender());

            return ResponseEntity.ok(userInfoDTO);
        } else {
            return new ResponseEntity<>("User is not logged in", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<User> alterUserInfo(String token, User user) throws NoSuchAlgorithmException {

        AuthenticationToken actualToken = iAuthRepo.findByTokenValue(token);
        User userBefore = actualToken.getUser();

        if (userBefore != null) {




            userBefore.setUserName(user.getUserName());
            userBefore.setUserEmail(user.getUserEmail());
            userBefore.setAddress(user.getAddress());
            userBefore.setPhoneNumber(user.getPhoneNumber());
            userBefore.setGender(user.getGender());
            userRepo.save(userBefore);


            return new ResponseEntity<>(userBefore, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<String> changePassword(String token, String oldPassword, String newPassword) throws NoSuchAlgorithmException {
        AuthenticationToken actualToken = iAuthRepo.findByTokenValue(token);

        if (actualToken != null) {
            User user = actualToken.getUser();
            String currentPassword = actualToken.getUser().getUserPassword();

            String encryptOldPassword = PasswordEncryptor.encrypt(oldPassword);


            if (currentPassword.equals(encryptOldPassword)) {
                String encryptedPass = PasswordEncryptor.encrypt(newPassword);
                user.setUserPassword(encryptedPass);
                userRepo.save(user);
                return new ResponseEntity<>("User password changed successfully!", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Old password doesn't match the current password.", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Invalid authentication token.", HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<String> customerServiceContactLoggedInUser(String subject, String token, String message) {
        if (authService.authenticateUserLoggedIn(token)) {

            AuthenticationToken tokenObj = iAuthRepo.findByTokenValue(token);

            String senderEmail = tokenObj.getUser().getUserEmail();

            String adminEmail = "vikash.kosaraju1234@gmail.com";
            JSONObject messageObj = new JSONObject(message);
            String messageContent = messageObj.getString("message");


            String fullMessage = "Received Message\n\n" +
                    "Subject: " + subject + "\n" +
                    "Sender Email: " + senderEmail + "\n\n" +
                    "Message:\n" + messageContent;
            MailHandlerBase.sendEmail(adminEmail, subject, fullMessage);


            return new ResponseEntity<>("message was swnt sucessfully", HttpStatus.OK);

        } else {
            return new ResponseEntity<>("something went wrong", HttpStatus.BAD_REQUEST);
        }

    }

    public ResponseEntity<String> guestCustomerService(String subject, String senderEmail, String message) {
        String adminEmail = "vikash.kosaraju1234@gmail.com";
        // Parse the JSON message to extract the message content
        JSONObject messageObj = new JSONObject(message);
        String messageContent = messageObj.getString("message");
        // Include sender's email in the message body

        String fullMessage = "Received Message\n\n" +
                "Subject: " + subject + "\n" +
                "Sender Email: " + senderEmail + "\n\n" +
                "Message:\n" + messageContent;
        MailHandlerBase.sendEmail(adminEmail, subject, fullMessage);
        return new ResponseEntity<>("Message was sent successfully", HttpStatus.OK);
    }
}



