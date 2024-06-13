package com.vikash.mobileCaseBackend.service;

import com.vikash.mobileCaseBackend.model.PasswordResetToken;
import com.vikash.mobileCaseBackend.model.User;
import com.vikash.mobileCaseBackend.repo.IRepoPasswordResetToken;
import com.vikash.mobileCaseBackend.repo.IRepoUser;
import com.vikash.mobileCaseBackend.service.EmailUtility.MailHandlerBase;
import com.vikash.mobileCaseBackend.service.HashingUtility.PasswordEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordService {
    @Autowired
    IRepoPasswordResetToken iPasswordResetToken;

    @Autowired
    IRepoUser userRepo;

    public ResponseEntity<String> resetPasswordToken(String email) {
        email = email.trim(); // Trim whitespace
        User userEmailCheck = userRepo.findByUserEmail(email);
        if (userEmailCheck != null) {
            User user = userRepo.findByUserEmail(email);
            Integer userId = user.getUserId();
            String userEmail = user.getUserEmail();
            String token = UUID.randomUUID().toString();

            PasswordResetToken passwordResetToken = new PasswordResetToken();
            passwordResetToken.setTokenValue(token);
            passwordResetToken.setUserId(userId);
            passwordResetToken.setTokenCreationDateTime(LocalDateTime.now());
            iPasswordResetToken.save(passwordResetToken); // Save token to database

            // Send email with token
            MailHandlerBase.sendEmail(userEmail, "Password Reset Token", token);

            return new ResponseEntity<>("Token was sent to your email. Please check.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Email is not registered.", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<String> resetPassword(String token, String email) throws NoSuchAlgorithmException {
        email = email.trim(); // Trim whitespace
        User user = userRepo.findByUserEmail(email);
        if (user == null) {
            return new ResponseEntity<>("Email is not found.", HttpStatus.NOT_FOUND);
        }

        // Check if there is a valid token associated with the user
        PasswordResetToken resetToken = iPasswordResetToken.findByUserIdAndTokenValue(user.getUserId(), token);
        if (resetToken == null || resetToken.getTokenCreationDateTime().isBefore(LocalDateTime.now().minusHours(1))) {
            return ResponseEntity.badRequest().body("Invalid or expired token.");
        }

        // Reset user's password to a temporary value (e.g., "null")
        String temporaryPassword = "null";
        String encryptedPass = PasswordEncryptor.encrypt(temporaryPassword);
        user.setUserPassword(encryptedPass);
        userRepo.save(user);


        String message = "Your password has been reset to a temporary value ('null'). "
                + "Please log in with this password and update it from your profile section.";

        // Send email notification about password reset
        MailHandlerBase.sendEmail(email, "New Temporary Password", message);

        // Delete the token from the database after resetting the password
        iPasswordResetToken.delete(resetToken);

        return ResponseEntity.ok("Password reset successfully. Log in with the temporary password and update it.");
    }
}