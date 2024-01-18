package com.vikash.mobileCaseBackend.repo;

import com.vikash.mobileCaseBackend.model.OrderEntity;
import com.vikash.mobileCaseBackend.model.Product;
import com.vikash.mobileCaseBackend.model.enums.Type;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IRepoProduct extends JpaRepository<Product,Integer> {



   List<Product> findProductByOrders(OrderEntity order);


    List<Product> findByProductAvailableAndProductName(boolean b, String productName);


















 List<Product> findProductAvailableByProductType(Type type);

 List<Product> findByProductAvailable(boolean b);

 List<Product> findProductAvailableByProductName(String productName);

 //List<Product> findProductAvailableByProductPriceLessThanEqual(double price);
 List<Product> findProductAvailableByProductTypeOrderByProductPriceDesc(Type type);

 List<Product> findProductAvailableByProductTypeOrderByProductPriceAsc(Type type);


 List<Product> findProductAvailableByProductTypeAndProductPriceLessThanEqual(Type type, double price);
}
