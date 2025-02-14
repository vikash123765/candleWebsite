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
import java.time.LocalDateTime;
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

    public ResponseEntity<Map<String, String>> userSignUp(User newUser) throws JsonProcessingException, NoSuchAlgorithmException {
        // Check if user already exists
        String newEmail = newUser.getUserEmail();
        User ifExistUser = userRepo.findByUserEmail(newEmail);
        if (ifExistUser != null) {
            if (ifExistUser.getUserEmail() != null && ifExistUser.getUserPassword() != null) {
                Map<String, String> responseBody = Map.of("message", "registered_user");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(responseBody);
            }

            if (ifExistUser.getUserEmail() != null) {

                String currentPassword = newUser.getUserPassword();
                String encryptedPass = PasswordEncryptor.encrypt(currentPassword);
                ifExistUser.setPhoneNumber(newUser.getPhoneNumber());
                ifExistUser.setUserEmail(ifExistUser.getUserEmail());
                ifExistUser.setUserName(newUser.getUserName());
                ifExistUser.setUserPassword(encryptedPass);
                ifExistUser.setAddress(newUser.getAddress());
                ifExistUser.setGender(newUser.getGender());

                userRepo.save(ifExistUser);

                Map<String, String> responseBody = Map.of("message", "account_created");
                return ResponseEntity.status(HttpStatus.CREATED).body(responseBody); // Using 201 Created status
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
        AuthenticationToken tokenObj = existingUser.getAuthenticationToken();

        if (tokenObj != null && tokenObj.getTokenValue() != null) {
            LocalDateTime tokenCreationDateTime = LocalDateTime.now();
            tokenObj.setTokenCreationDateTime(tokenCreationDateTime);
            authService.saveToken(tokenObj);

            // Create a cookie header with the existing token value
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Token", "token=" + tokenObj.getTokenValue());
            headers.add("Access-Control-Expose-Headers", "X-Token");
            headers.add("Access-Control-Allow-Headers", "X-Token");

            return new ResponseEntity<>("Login successful!", headers, HttpStatus.OK);

        } else {

            try {
                String encryptedPassword = PasswordEncryptor.encrypt(password);


                if (existingUser.getUserPassword().equals(encryptedPassword)) {

                    AuthenticationToken token = new AuthenticationToken(existingUser);

                    LocalDateTime tokenCreationDateTime = LocalDateTime.now();

                    token.setTokenCreationDateTime(tokenCreationDateTime);  // Set the token creation time
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

            String adminEmail = "vtscustomersupp@gmail.com";
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
        String adminEmail = "vtscustomersupp@gmail.com";
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



/*  public ResponseEntity<String> getUserToken(String userEmail) {

      User user = userRepo.findByUserEmail(userEmail);
      String tokenValue = user.getAuthenticationToken().getTokenValue();

      if (authService.authenticate(userEmail, tokenValue)) {
          if (tokenValue != null && tokenValue.equals(userEmail)) {
              return new ResponseEntity<>("this is the" + token + "value for : " + userEmail, HttpStatus.OK);
          } return new ResponseEntity<>("there is not token value fr this email", HttpStatus.NOT_FOUND);
      }
  }*/
}



