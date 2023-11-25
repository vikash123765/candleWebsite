
package com.vikash.instBackend.service;


import com.vikash.instBackend.dto.OrderEntityWithProductsDTO;
import com.vikash.instBackend.model.OrderEntity;
import com.vikash.instBackend.model.Product;
import com.vikash.instBackend.model.User;
import com.vikash.instBackend.repo.IRepoOrder;
import com.vikash.instBackend.repo.IRepoProduct;
import com.vikash.instBackend.repo.IRepoUser;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderEntityService {

    @Autowired
    IRepoUser repoUser;

    @Autowired
    IRepoProduct repoProduct;

    @Autowired
    IRepoOrder repoOrder;

    public String placingOrder(Integer userId, List<Integer> productIds) {
        User user = repoUser.findById(userId).orElseThrow();

        // Create a new OrderEntity with the User
        OrderEntity order = new OrderEntity();
        order.setUser(user);

        // Save the order to generate the orderNumber
        order.setSetCreatingTimeStamp(LocalDateTime.now());
        repoOrder.save(order);

        // The generated orderNumber is now retrieved
        Integer generatedOrderNumber = order.getOrderNumber();

        // Associate the order with the products and save the products
        for (Integer productId : productIds) {
            Product product = repoProduct.findById(productId).orElseThrow();
            product.setOrderEntity(order);
            repoProduct.save(product); // Save each product after associating with the order
        }

        return "Order placed successfully!! Order Number: " + generatedOrderNumber;
    }

    public List<OrderEntity> getOrderHistory(Integer userId) {
        User user = repoUser.findById(userId).orElseThrow();
        return repoOrder.findByUserOrderByOrderNumberDesc(user);

    }

    // public List<OrderEntity> getOrdersWithProductsByUserId(Integer userId) {
      //  return repoOrder.findByUserIdWithProducts(userId);
   // }

    public List<OrderEntityWithProductsDTO> getOrderHistoryWithProducts(Integer userId) {
        List<OrderEntity> orderHistory = repoOrder.findByUserUserId(userId);
        List<OrderEntityWithProductsDTO> result = new ArrayList<>();

        for (OrderEntity order : orderHistory) {
            List<Product> products = repoProduct.findByOrderEntity(order);

            OrderEntityWithProductsDTO orderWithProducts = new OrderEntityWithProductsDTO(order, products);
            result.add(orderWithProducts);
        }

        return result;
    }
}
