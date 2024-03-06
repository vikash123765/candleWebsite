package com.vikash.mobileCaseBackend.repo;

import com.vikash.mobileCaseBackend.model.AuthenticationToken;
import com.vikash.mobileCaseBackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAuthRepo extends JpaRepository<AuthenticationToken,Long> {


  /*  static AuthenticationToken findTokenValueByUser(User existingUser) {
        // it should not check if existing user is null rather the token value for tje existing user
        if (existingUser != null) {
            return new AuthenticationToken(existingUser); // Assuming AuthenticationToken constructor takes a User object.
        } else {
            return null;
        }
    }*/



    AuthenticationToken findByTokenValue(String tokenValue);

    //User findUserEmailByTokenValue(String token);



   // Admin findAdminByTokenValue(String token);

  //  User findUserByTokenValue(String token);

    //AuthenticationToken findTokenValueByAdmin(String adminEmail);
}
