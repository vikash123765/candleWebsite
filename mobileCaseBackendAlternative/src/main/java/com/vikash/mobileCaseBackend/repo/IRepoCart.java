package com.vikash.mobileCaseBackend.repo;

import com.vikash.mobileCaseBackend.model.Cart;
import com.vikash.mobileCaseBackend.model.OrderEntity;
import com.vikash.mobileCaseBackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRepoCart extends JpaRepository<Cart,Integer> {
    Cart findByUser(User user);
}
