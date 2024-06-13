package com.vikash.mobileCaseBackend.repo;

import com.vikash.mobileCaseBackend.model.OrderEntity;
import com.vikash.mobileCaseBackend.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRepoPasswordResetToken  extends JpaRepository<PasswordResetToken,Long> {

    PasswordResetToken findByUserIdAndTokenValue(Integer userId, String token);
}
