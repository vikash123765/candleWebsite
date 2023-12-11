
package com.vikash.mobileCaseBackend.service;
import com.vikash.mobileCaseBackend.model.*;
import com.vikash.mobileCaseBackend.repo.IRepoOrder;
import com.vikash.mobileCaseBackend.repo.IRepoProduct;
import com.vikash.mobileCaseBackend.repo.IRepoUser;
import jakarta.transaction.Transactional;
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

    @Autowired
    AuthService authenticationService;

    @Autowired
    ProductService productService;



    public List<Map<String, Object>> getOrderHistoryByUserId(String email, String tokenValue) {
        if (authenticationService.authenticate(email, tokenValue)) {

            // figure out the actual user  with email
            User user = repoUser.findByUserEmail(email);

            // figure out the actual orders of that user
            List<OrderEntity> orderTobeAcessed = repoOrder.findOrderByUser(user);


            if(authorizeOrderHistoryAccesser(email,orderTobeAcessed)) {


                List<Map<String, Object>> orderList = new ArrayList<>();

                for (OrderEntity order : orderTobeAcessed) {
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("orderId", order.getOrderNumber());
                    orderMap.put("userName", order.getUser().getUserName());
                    orderMap.put("order placed : ",order.getSetCreatingTimeStamp());
                    //orderMap.put("delivered", order.get);
                    // orderMap.put("sent", order.());

                    // Fetch products associated with the order via repository query
                    List<Product> products = repoProduct.findProductByOrderEntity(order);
                    List<Map<String, Object>> productDetails = new ArrayList<>();

                    for (Product product : products) {
                        Map<String, Object> productMap = new HashMap<>();
                        productMap.put("productName", product.getProductName());
                        productMap.put("productType", product.getProductType());
                        productMap.put("productPrice", product.getProductPrice());



                        productDetails.add(productMap);
                    }

                    orderMap.put("products", productDetails);
                    orderList.add(orderMap);
                }

                return orderList;
            }

            else{
                return Collections.singletonList(Collections.singletonMap("message", "Unuthorized access"));
            }

        } else {
            // Return a message indicating unauthenticated access
            return Collections.singletonList(Collections.singletonMap("message", "Unauthenticated access"));
        }

    }


    private boolean authorizeOrderHistoryAccesser(String email, List<OrderEntity> orderTobeAcessed) {
        User potentialAccesser = repoUser.findByUserEmail(email);
        for (OrderEntity order : orderTobeAcessed) {
            if (order.getUser().getUserEmail().equals(potentialAccesser.getUserEmail())) {
                return true;
            }
        }
        return false;
    }


    public String markOrderAsSent(String email,String tokenValue,Integer orderNr) {
        if (authenticationService.authenticate(email, tokenValue)) {

            OrderEntity order = repoOrder.findByOrderNumber(orderNr);
            if(!order.isMarkAsSent()) {
                order.setMarkAsSent(true);
                repoOrder.save(order);
                return "order with  order number : " + orderNr + "is marked as done";
            }else{
                return "order already sent";
            }

        } else {
            return "Un Authenticated access!!!";
        }


    }

    public String markOrderAsDelivered(String email, String tokenValue,Integer orderNr) {
        if (authenticationService.authenticate(email, tokenValue)) {
        OrderEntity order = repoOrder.findByOrderNumber(orderNr);
        if(!order.isMarkAsDelivered()){
            order.setMarkAsDelivered(true);
            repoOrder.save(order);
            return "order with  order number : " + orderNr + "is marked as done";
        }else{
            return "order already sent";
        }
        } else {
            return "Un Authenticated access!!!";
        }

    }
    @Transactional
    public String processGuestOrder(GuestOrderRequestAndProducts requestAndProducts) {

            GuestOrderRequest guestOrderRequest = requestAndProducts.getGuestOrderRequest();
            List<Integer> productIds = requestAndProducts.getProductIds();



        // Create a new user
        User user = new User();

        user.setUserName(guestOrderRequest.getFullName());
        user.setUserEmail(guestOrderRequest.getEmail());
        user.setAddress(guestOrderRequest.getShippingAddress());
        user.setPhoneNumber(guestOrderRequest.getPhoneNumber());
        // Save the user to the database
        User savedUser = repoUser.save(user);

        // Create a new order
        OrderEntity order = new OrderEntity();
        order.setUser(savedUser);

        // Set other order details and save to the database
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

        return  "Order placed successfully!! Order Number: " + generatedOrderNumber;

    }

    public String placingOrder(String email, String token, List<Integer> productIds) {


        if (authenticationService.authenticate(email, token)) {
            // fins uder with email
            User user = repoUser.findByUserEmail(email);

            // Create a new object of OrderEntity with the User
            OrderEntity order = new OrderEntity();
            // order.getProduct().setQuantity();
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

        } else {
            return "Un Authenticated access!!!";
        }

    }

}


