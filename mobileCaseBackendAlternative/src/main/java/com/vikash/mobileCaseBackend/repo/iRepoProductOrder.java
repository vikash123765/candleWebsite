package com.vikash.mobileCaseBackend.repo;

import com.vikash.mobileCaseBackend.model.Product;
import com.vikash.mobileCaseBackend.model.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface iRepoProductOrder extends JpaRepository<ProductOrder,Integer> {

    // Custom query to fetch productId, orderId, and quantity of products in orders
    @Query("SELECT po.product.productId as productId, po.orders.orderNumber as orderId, COUNT(po) as quantity " +
            "FROM ProductOrder po " +
            "WHERE po.orders.orderNumber = :orderId " +
            "GROUP BY po.product.productId, po.orders.orderNumber")
    List<Object[]> findProductOrderQuantities(@Param("orderId") Integer orderId);



    @Query("SELECT po.product.productId as productId, COUNT(po) as quantity " +
            "FROM ProductOrder po " +
            "GROUP BY po.product.productId")
    List<Object[]> findProductOrderQuantities();
}
