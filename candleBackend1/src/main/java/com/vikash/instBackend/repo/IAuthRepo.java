package com.vikash.instBackend.repo;

import com.vikash.instBackend.model.AuthenticationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAuthRepo extends JpaRepository<AuthenticationToken,Long> {
    AuthenticationToken findByTokenValue(String tokenValue);
}
