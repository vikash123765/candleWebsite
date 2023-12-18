
package com.vikash.mobileCaseBackend.service;
import com.vikash.mobileCaseBackend.model.*;
import com.vikash.mobileCaseBackend.repo.*;
import com.vikash.mobileCaseBackend.service.EmailUtility.SendMailOrderInfo;
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
    CartService cartService;

    @Autowired
    CartItemService cartItemService;

    @Autowired
    SendMailOrderInfo sendMailOrderInfo;



    @Autowired
    IRepoCart repoCart;

    @Autowired
    IRepoGuestCart iRepoGuestCart;
    @Autowired
    IRepoGuestCartItem iRepoGuestCartItem;



    public List<Map<String, Object>> getOrderHistoryByUserEmail(String email, String tokenValue) {
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


    public String markOrderAsSent(String email, String tokenValue, Integer orderNr, Integer trackingId) {
        if (authenticationService.authenticate(email, tokenValue)) {
            OrderEntity order = repoOrder.findByOrderNumber(orderNr);

            // Check if trackingId is provided
            if (trackingId != null) {
                order.setTrackingNumber(trackingId);
            }

            if (!order.isMarkAsSent()) {
                order.setMarkAsSent(true);
                repoOrder.save(order);

                // Send email notification
                String subject = "Order Marked as Sent";
                String body = "Your order with order number " + orderNr + " has been marked as sent.";

                // Include tracking ID in the email body if available
                if (trackingId != null) {
                    body += "\nTracking ID: " + trackingId;
                }

                body += "\nThank you for shopping with us!";

                sendMailOrderInfo.sendEmail(order.getUser().getUserEmail(), subject, body, order);

                // You can also notify the admin if needed
                // emailService.sendEmail(adminEmail, subject, body, order);

                return "Order with order number: " + orderNr + " is marked as sent";
            } else {
                return "Order already sent";
            }
        } else {
            return "Unauthenticated access!!!";
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


    public String finalizeOrder(String email, String token) {
        if (authenticationService.authenticate(email, token)) {
            User user = repoUser.findByUserEmail(email);
            Cart cart = cartService.getCartByUser(user);
            List<CartItem> cartItems = cart.getCartItems();

            // Check if the cart has items
            if (cartItems.isEmpty()) {
                return "Cart is empty. Cannot finalize order.";
            }

            // Create and populate an OrderEntity
            OrderEntity order = new OrderEntity();
            order.setUser(user);
            order.setSetCreatingTimeStamp(LocalDateTime.now());

            // Save the order to the database first
            repoOrder.save(order);

            // Retrieve the products from the cart items
            for (CartItem cartItem : cartItems) {
                Product orderProduct = cartItem.getProduct();

                // Mark the product as reserved (not available) in the order
                orderProduct.setReservationTime(LocalDateTime.now());

                // Set the product's orderEntity reference to the saved order
                orderProduct.setOrderEntity(order);

                // add boolen check if payment is sucessfull or not
                //respone from paypal


                // Save the product to associate it with the new order
                repoProduct.save(orderProduct);
            }

            // Optionally mark the cart as having the order placed
            cart.setOrderPlaced(true);
            repoCart.save(cart);

            // Reset the user's cart after the order is finalized
            cartService.resetCart(user);

            // Send email notifications
            String userSubject = "Order Placed";
            String userBody = "Your order has been placed. Thank you for shopping with us!";
            sendMailOrderInfo.sendEmail(user.getUserEmail(), userSubject, userBody, order);

            String adminEmail = "admin@example.com"; // Replace with your actual admin email
            String adminSubject = "New Order Placed";
            String adminBody = "A new order has been placed. Order Number: " + order.getOrderNumber();
            sendMailOrderInfo.sendEmail(adminEmail, adminSubject, adminBody, order);

            return "Order finalized successfully!";
        } else {
            return "Unauthorized access";
        }
    }
    public String finalizeGuestOrder(Integer guestCartId, GuestOrderRequest guestOrderRequest) {
        // Create a new guest user
        User guestUser = new User();
        guestUser.setUserName(guestOrderRequest.getFullName());
        guestUser.setUserEmail(guestOrderRequest.getEmail());
        guestUser.setAddress(guestOrderRequest.getShippingAddress());
        guestUser.setPhoneNumber(guestOrderRequest.getPhoneNumber());

        // Save the guest user to the database
        User savedGuestUser = repoUser.save(guestUser);

        // Create and populate a GuestOrderEntity
        OrderEntity guestOrder = new OrderEntity();
        guestOrder.setUser(savedGuestUser);
        guestOrder.setMarkAsSent(false); // Set default values for other order-related fields
        guestOrder.setMarkAsDelivered(false);
        //guestOrder.setOrderNumber(0); // Set a default value for order number
        guestOrder.setSetCreatingTimeStamp(LocalDateTime.now());
        // Save the order to the database
        repoOrder.save(guestOrder);

        // Retrieve the products from the guest cart items
        GuestCart guestCart = iRepoGuestCart.findById(guestCartId).orElse(null);
        if (guestCart != null) {
            List<GuestCartItem> guestCartItems = guestCart.getGuestCartItems();

            for (GuestCartItem guestCartItem : guestCartItems) {
                Product orderProduct = guestCartItem.getProduct();


                // Save the product to associate it with the new order
                orderProduct.setOrderEntity(guestOrder);
                repoProduct.save(orderProduct);
            }


            // Clear the guest cart items
            guestCartItems.clear();
            iRepoGuestCartItem.deleteAll(guestCartItems);

            // Send email notifications
            String userSubject = "Guest Order Placed";
            String userBody = "Your guest order has been placed. Thank you for shopping with us!";
            sendMailOrderInfo.sendEmail(savedGuestUser.getUserEmail(), userSubject, userBody, guestOrder);

            String adminEmail = "admin@example.com"; // Replace with your actual admin email
            String adminSubject = "New Guest Order Placed";
            String adminBody = "A new guest order has been placed. Order Number: " + guestOrder.getOrderNumber();
            sendMailOrderInfo.sendEmail(adminEmail, adminSubject, adminBody, guestOrder);


            return "Guest order finalized successfully!";
        } else {
            return "Guest cart not found. Order finalization failed.";
        }
    }


/*    private boolean checkPaymentStatus() {

    }*/
}


