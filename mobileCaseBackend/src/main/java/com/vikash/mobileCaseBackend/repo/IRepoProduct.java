package com.vikash.mobileCaseBackend.repo;

import com.vikash.mobileCaseBackend.model.OrderEntity;
import com.vikash.mobileCaseBackend.model.Product;
import com.vikash.mobileCaseBackend.model.enums.Type;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IRepoProduct extends JpaRepository<Product,Integer> {














    List<Product> findByProductAvailable(boolean b);

    List<Product> findByProductAvailableAndProductType(boolean b, Type type);

    List<Product> findByProductName(String name);

    List<Product> findByProductPriceLessThanEqualAndProductType( double price,Type type);


    List<Product> findByProductTypeOrderByProductPriceDesc(Type type);

    List<Product> findByProductTypeOrderByProductPriceAsc(Type type);


    List<Product> findProductByOrderEntity(OrderEntity order);
}
