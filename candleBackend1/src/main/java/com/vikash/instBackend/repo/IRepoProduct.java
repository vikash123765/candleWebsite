package com.vikash.instBackend.repo;

import com.vikash.instBackend.model.OrderEntity;
import com.vikash.instBackend.model.Product;
import com.vikash.instBackend.model.enums.Type;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IRepoProduct extends JpaRepository<Product,Integer> {














    List<Product> findByProductAvailable(boolean b);

    List<Product> findByProductAvailableAndProductType(boolean b, Type type);

    List<Product> findByProductName(String name);

    List<Product> findByProductPriceLessThanEqualAndProductType( double price,Type type);


    List<Product> findByProductTypeOrderByProductPriceDesc(Type type);

    List<Product> findByProductTypeOrderByProductPriceAsc(Type type);


    List<Product> findByOrderEntity(OrderEntity order);
}
