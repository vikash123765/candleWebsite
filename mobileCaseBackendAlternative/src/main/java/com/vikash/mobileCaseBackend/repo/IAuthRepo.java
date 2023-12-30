package com.vikash.mobileCaseBackend.repo;

import com.vikash.mobileCaseBackend.model.AuthenticationToken;
import com.vikash.mobileCaseBackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAuthRepo extends JpaRepository<AuthenticationToken,Long> {


    static AuthenticationToken findTokenValueByUser(User existingUser) {
        if (existingUser != null) {
            return new AuthenticationToken(existingUser); // Assuming AuthenticationToken constructor takes a User object.
        } else {
            return null;
        }
    }



    AuthenticationToken findByTokenValue(String tokenValue);
}
