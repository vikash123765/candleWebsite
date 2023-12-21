package com.vikash.mobileCaseBackend.repo;

import com.vikash.mobileCaseBackend.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRepoCartItem extends JpaRepository<CartItem,Integer> {

}
