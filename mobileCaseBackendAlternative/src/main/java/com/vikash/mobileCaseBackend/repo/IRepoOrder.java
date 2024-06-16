package com.vikash.mobileCaseBackend.repo;

import com.vikash.mobileCaseBackend.model.OrderEntity;
import com.vikash.mobileCaseBackend.model.Product;
import com.vikash.mobileCaseBackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IRepoOrder extends JpaRepository<OrderEntity,Integer> {






    OrderEntity findByOrderNumber(Integer orderNr);










    List<OrderEntity> findOrdersByUser(User user);


}
