package com.vikash.mobileCaseBackend.repo;

import com.vikash.mobileCaseBackend.model.CartItem;
import com.vikash.mobileCaseBackend.model.GuestCart;
import com.vikash.mobileCaseBackend.model.GuestCartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IRepoGuestCart extends JpaRepository<GuestCart,Integer> {
    GuestCart findBySessionToken(String sessionToken);

}
