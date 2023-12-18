package com.vikash.mobileCaseBackend.repo;

import com.vikash.mobileCaseBackend.model.CartItem;
import com.vikash.mobileCaseBackend.model.GuestCart;
import com.vikash.mobileCaseBackend.model.GuestCartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRepoGuestCart extends JpaRepository<GuestCart,Integer> {
}
