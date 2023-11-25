package com.vikash.instBackend.repo;

import com.vikash.instBackend.model.OrderEntity;
import com.vikash.instBackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IRepoOrder extends JpaRepository<OrderEntity,Integer> {
    List<OrderEntity> findByUserOrderByOrderNumberDesc(User user);

    //List<OrderEntity> findByUserIdWithProducts(Integer userId);

    List<OrderEntity> findByUserUserId(Integer userId);
}
