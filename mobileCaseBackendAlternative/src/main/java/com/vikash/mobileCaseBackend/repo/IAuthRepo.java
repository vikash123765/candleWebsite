package com.vikash.mobileCaseBackend.repo;

import com.vikash.mobileCaseBackend.model.AuthenticationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAuthRepo extends JpaRepository<AuthenticationToken,Long> {
    AuthenticationToken findByTokenValue(String tokenValue);
}
