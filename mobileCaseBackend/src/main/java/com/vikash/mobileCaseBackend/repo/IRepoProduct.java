package com.vikash.mobileCaseBackend.repo;

import com.vikash.mobileCaseBackend.model.OrderEntity;
import com.vikash.mobileCaseBackend.model.Product;
import com.vikash.mobileCaseBackend.model.enums.Type;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IRepoProduct extends JpaRepository<Product,Integer> {



    List<Product> findProductByOrderEntity(OrderEntity order);


    List<Product> findByProductAvailableAndProductName(boolean b, String productName);





    List<Product> findFirstProductAvailableByProductType(Type type);


    List<Product> findFirstProductAvailableByProductName(String productName);


    List<Product> findFirstProductAvailableByProductPriceLessThanEqual(double price);

    List<Product> findFirstProductAvailableByProductTypeOrderByProductPriceDesc(Type type);

    List<Product> findFirstProductAvailableByProductTypeOrderByProductPriceAsc(Type type);


    List<Product> findFirstProductAvailableByProductAvailable(boolean b);
}
