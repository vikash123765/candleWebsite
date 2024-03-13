
package com.vikash.mobileCaseBackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vikash.mobileCaseBackend.model.*;
import com.vikash.mobileCaseBackend.repo.*;
import com.vikash.mobileCaseBackend.service.EmailUtility.SendMailOrderInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    IAuthRepo authRepo;


    @Autowired
    SendMailOrderInfo sendMailOrderInfo;



    @Autowired
    IRepoCart repoCart;

    @Autowired
    IRepoGuestCart iRepoGuestCart;
    @Autowired
    IRepoGuestCartItem iRepoGuestCartItem;



    public List<Map<String, Object>> getOrderHistoryByUserEmail(String token) {


        // figure out the actual token with tokenvalue
        AuthenticationToken actualToken = authRepo.findByTokenValue(token);

        // Check if actualToken is not null
        if (actualToken != null) {
            // figure out the email of the user
            String email = actualToken.getUser().getUserEmail();
            // we get the user here
            User user = actualToken.getUser();

            // figure out the actual orders of that user
            List<OrderEntity> ordersTobeAccessed = repoOrder.findOrdersByUser(user);

            if (ordersTobeAccessed == null) {
                return Collections.singletonList(Collections.singletonMap("message", user.getUserName() + " does not have any orders"));
            }


            if (authorizeOrderHistoryAccesser(email, ordersTobeAccessed)) {
                List<Map<String, Object>> orderList = new ArrayList<>();

                for (OrderEntity order : ordersTobeAccessed) {
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("orderId", order.getOrderNumber());
                    orderMap.put("userName", order.getUser().getUserName());
                    orderMap.put("sent", order.isMarkAsSent());
                    orderMap.put("orderCreated", order.getCreationTimeStamp());
                    orderMap.put("delivered", order.isMarkAsDelivered());
                    orderMap.put("trackingId", order.getTrackingNumber());
                    // Fetch products associated with the order via repository query
                    List<Product> products = repoProduct.findProductByOrders(order);
                    List<Map<String, Object>> productDetails = new ArrayList<>();
                    double totalOrderCost = 0.0;

                    for (Product product : products) {
                        Map<String, Object> productMap = new HashMap<>();
                        productMap.put("productName", product.getProductName());
                        productMap.put("productType", product.getProductType());
                        productMap.put("productPrice", product.getProductPrice());

                        totalOrderCost += product.getProductPrice();
                        productDetails.add(productMap);
                    }

                    orderMap.put("products", productDetails);
                    orderMap.put("total Cost", totalOrderCost);
                    orderList.add(orderMap);
                }

                return orderList;
            } else {
                return Collections.singletonList(Collections.singletonMap("message", "Unauthorized access"));
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



    public ResponseEntity<String> markOrderAsSent(String email, String tokenValue, Integer orderNr, Integer trackingId) {
        if (authenticationService.authenticate(email, tokenValue)) {

            OrderEntity order = repoOrder.findByOrderNumber(orderNr);

            if (order == null){
                return new ResponseEntity<>( "order does not exist with order number :" + orderNr,HttpStatus.NOT_FOUND);
            }
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
                String adminEmail="vikash.kosaraju1234@gmail.com";
                sendMailOrderInfo.sendEmail(adminEmail, subject, body, order);

                return new ResponseEntity<>( "Order with order number: " + orderNr + " is marked as sent", HttpStatus.OK);
            } else {
                return new ResponseEntity<>( "Order already sent",HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>( "Unauthenticated access!!!",HttpStatus.UNAUTHORIZED);
        }
    }


    public ResponseEntity<String> markOrderAsDelivered(String email, String tokenValue,Integer orderNr) {
        if (authenticationService.authenticate(email, tokenValue)) {
            OrderEntity order = repoOrder.findByOrderNumber(orderNr);
            if(order == null){
                return new ResponseEntity<>( "order with order number : " +orderNr +"does not exist",HttpStatus.NOT_FOUND);
            }
            if(!order.isMarkAsDelivered()){
                order.setMarkAsDelivered(true);
                repoOrder.save(order);
                // Send email notification
                String subject = "Order Marked as delivered";
                String body = "Your order with order number " + orderNr + " has been marked as delivered.";

                sendMailOrderInfo.sendEmail(order.getUser().getUserEmail(), subject, body, order);


                String adminEmail="vikash.kosaraju1234@gmail.com";
                sendMailOrderInfo.sendEmail(adminEmail, subject, body, order);
                return  new ResponseEntity<>("order with  order number : " + orderNr + "is delivered",HttpStatus.OK);
            }else{
                return new ResponseEntity<>( "Order already delivered",HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>("Un Authenticated access!!!",HttpStatus.UNAUTHORIZED);
        }

    }


  /*  public String finalizeOrder(String email, String token) {
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
            order.setCreationTimeStamp(LocalDateTime.now());

            // Save the order to the database first
            repoOrder.save(order);

            // Create a separate list to collect products
            List<Product> productsToUpdate = new ArrayList<>();

            // Retrieve the products from the cart items
            for (CartItem cartItem : cartItems) {
                Product orderProduct = cartItem.getProduct();

                // Set the product's orderEntity reference to the saved order
                orderProduct.getOrders().add(order);
                productsToUpdate.add(orderProduct);
            }

            // Save all products after modifying relationships
            repoProduct.saveAll(productsToUpdate);

            // Update the user's orders outside the loop
            user.getOrders().add(order);
            repoUser.save(user);

            // Optionally mark the cart as having the order placed
            cart.setOrderPlaced(true);
            repoCart.save(cart);

            // booleam check

            // Reset the user's cart after the order is finalized
            cartService.resetCart(user);

            // Send email notifications
            String userSubject = "Order Placed";
            String userBody = "Your order has been placed. Thank you for shopping with us!";
            sendMailOrderInfo.sendEmail(user.getUserEmail(), userSubject, userBody, order);

            String adminEmail = "vikash.kosaraju1234@gmail.com"; // Replace with your actual admin email
            String adminSubject = "New Order Placed";
            String adminBody = "A new order has been placed. Order Number: " + order.getOrderNumber();
            sendMailOrderInfo.sendEmail(adminEmail, adminSubject, adminBody, order);

            return "Order finalized successfully!";
        } else {
            return "Unauthorized access";
        }
    }*/


    public ResponseEntity<String> finalizeOrder(String token, String jsonPayload) {
        if (authenticationService.authenticateUserLoggedIn(token)) {
            AuthenticationToken tokenObj = authRepo.findByTokenValue(token);
            User user = tokenObj.getUser();

            // Parse the JSON payload into a List of product IDs
            List<Integer> productIds;
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                productIds = objectMapper.readValue(jsonPayload, new TypeReference<List<Integer>>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error parsing JSON payload", e);
            }

            // Create and populate an OrderEntity
            OrderEntity order = new OrderEntity();
            order.setUser(user);
            order.setCreationTimeStamp(LocalDateTime.now());

            // Save the order to the database
            repoOrder.save(order);

            // Retrieve products by productIds and create associations with the order
            for (Integer productId : productIds) {
                Product product = repoProduct.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found for ID: " + productId));

                // Add the product to the order's products list
                order.getProducts().add(product);
            }

            // Save the order with associated products

            repoOrder.save(order);

            // Update the user's orders
            user.getOrders().add(order);

            // i need a boolean check here to only save if payment is sucessfull 
            repoUser.save(user);

            // Send email notifications
            String userSubject = "Order Placed";
            String userBody = "Your order has been placed. Thank you for shopping with us!";
            sendMailOrderInfo.sendEmail(user.getUserEmail(), userSubject, userBody, order);

            String adminEmail = "vikash.kosaraju1234@gmail.com";
            String adminSubject = "New Order Placed";
            String adminBody = "A new order has been placed. Order Number: " + order.getOrderNumber();
            sendMailOrderInfo.sendEmail(adminEmail, adminSubject, adminBody, order);

            return new ResponseEntity<>( "Order finalized successfully!",HttpStatus.OK);
        } else {
            return  new ResponseEntity<>("Unauthorized access",HttpStatus.UNAUTHORIZED);
        }
    }






  /*  public String finalizeGuestOrder(GuestOrderRequest guestOrderRequest) {
        // Create a new guest user
        User guestUser = new User();
        guestUser.setUserName(guestOrderRequest.getUserName());
        guestUser.setUserEmail(guestOrderRequest.getEmail());
        guestUser.setAddress(guestOrderRequest.getShippingAddress());
        guestUser.setPhoneNumber(guestOrderRequest.getPhoneNumber());

        // Save the guest user to the database
        User savedGuestUser = repoUser.save(guestUser);

        // Create and populate a GuestOrderEntity
        OrderEntity guestOrder = new OrderEntity();
        guestOrder.setMarkAsSent(false); // Set default values for other order-related fields
        guestOrder.setMarkAsDelivered(false);
        guestOrder.setCreationTimeStamp(LocalDateTime.now());

        // Link the guest order with the guest user
        guestOrder.setUser(savedGuestUser);

        // Save the order to the database
        repoOrder.save(guestOrder);

        // Retrieve the products from the guest cart items
       // GuestCart guestCart = iRepoGuestCart.findById(guestCartId).orElse(null);
        // Generate or fetch session token internally (pseudo code, you'll need to implement this)
        String sessionToken = generateOrFetchSessionToken(savedGuestUser);
        GuestCart guestCart = iRepoGuestCart.findBySessionToken(sessionToken);
        if (guestCart != null) {
            List<GuestCartItem> guestCartItems = guestCart.getGuestCartItems();

            for (GuestCartItem guestCartItem : guestCartItems) {
                Product orderProduct = guestCartItem.getProduct();

                // Link the product with the guest order
                orderProduct.getOrders().add(guestOrder);
                // Link the product with the guest user
                orderProduct.getUsers().add(savedGuestUser);

                // Save the product to associate it with the new order
                repoProduct.save(orderProduct);
            }

            // Link the guest order with the guest user's orders
            savedGuestUser.getOrders().add(guestOrder);
            repoUser.save(savedGuestUser);

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
    }*/

    public String finalizeGuestOrder(GuestOrderRequest guestOrderRequest, String jsonPayload) {
        String passedEmail = guestOrderRequest.getEmail();

        // Check if the user with the provided email already exists
        User existingUser = repoUser.findByUserEmail(passedEmail);


        if (existingUser != null) {
            // User already exists, link the existing user with the new order and products


            // Create and populate a GuestOrderEntity
            OrderEntity guestOrder = new OrderEntity();
            guestOrder.setMarkAsSent(false); // Set default values for other order-related fields
            guestOrder.setMarkAsDelivered(false);
            guestOrder.setCreationTimeStamp(LocalDateTime.now());

            // Link the guest order with the existing user
            guestOrder.setUser(existingUser);

            // Save the order to the database
            repoOrder.save(guestOrder);

            // Parse the JSON payload into a List of product IDs
            List<Integer> productIds;
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                productIds = objectMapper.readValue(jsonPayload, new TypeReference<List<Integer>>() {
                });
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error parsing JSON payload", e);
            }

            // Retrieve products by productIds and create associations with the order
            for (Integer productId : productIds) {
                Product product = repoProduct.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found for ID: " + productId));

                // Add the product to the order's products list
                guestOrder.getProducts().add(product);
            }

            // Update the user's orders
            existingUser.getOrders().add(guestOrder);
            repoUser.save(existingUser);

            // Send email notifications
            String userSubject = "Guest Order Placed";
            String userBody = "Your guest order has been placed. Thank you for shopping with us!";
            sendMailOrderInfo.sendEmail(existingUser.getUserEmail(), userSubject, userBody, guestOrder);

            String adminEmail = "admin@example.com"; // Replace with your actual admin email
            String adminSubject = "New Guest Order Placed";
            String adminBody = "A new guest order has been placed. Order Number: " + guestOrder.getOrderNumber();
            sendMailOrderInfo.sendEmail(adminEmail, adminSubject, adminBody, guestOrder);

            return "Guest order finalized successfully!";


        } else {

            // Create a new guest user
            // User doesn't exist, proceed with creating a new guest user and order
            User guestUser = new User();
            guestUser.setUserName(guestOrderRequest.getUserName());
            guestUser.setUserEmail(guestOrderRequest.getEmail());
            guestUser.setAddress(guestOrderRequest.getShippingAddress());
            guestUser.setPhoneNumber(Long.valueOf(String.valueOf(guestOrderRequest.getPhoneNumber())));

            // Save the guest user to the database
            User savedGuestUser = repoUser.save(guestUser);

            // Create and populate a GuestOrderEntity
            OrderEntity guestOrder = new OrderEntity();
            guestOrder.setMarkAsSent(false); // Set default values for other order-related fields
            guestOrder.setMarkAsDelivered(false);
            guestOrder.setCreationTimeStamp(LocalDateTime.now());

            // Link the guest order with the guest user
            guestOrder.setUser(savedGuestUser);

            // Save the order to the database
            repoOrder.save(guestOrder);

            // Parse the JSON payload into a List of product IDs
            List<Integer> productIds;
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                productIds = objectMapper.readValue(jsonPayload, new TypeReference<List<Integer>>() {
                });
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error parsing JSON payload", e);
            }

            // Retrieve products by productIds and create associations with the order
            for (Integer productId : productIds) {
                Product product = repoProduct.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found for ID: " + productId));

                // Add the product to the order's products list
                guestOrder.getProducts().add(product);
            }

            // Update the user's orders
            savedGuestUser.getOrders().add(guestOrder);
            repoUser.save(savedGuestUser);

            // Send email notifications and other necessary actions...
            // Send email notifications
            String userSubject = "Guest Order Placed";
            String userBody = "Your guest order has been placed. Thank you for shopping with us!";
            sendMailOrderInfo.sendEmail(savedGuestUser.getUserEmail(), userSubject, userBody, guestOrder);

            String adminEmail = "admin@example.com"; // Replace with your actual admin email
            String adminSubject = "New Guest Order Placed";
            String adminBody = "A new guest order has been placed. Order Number: " + guestOrder.getOrderNumber();
            sendMailOrderInfo.sendEmail(adminEmail, adminSubject, adminBody, guestOrder);

            return "Guest order finalized successfully!";


        }
    }

/*    public ResponseEntity<Map<String, Object>> calcualteShippingCost(boolean swedenOrNot, boolean tracableOrNonTracable, double packageWeight) {

        Map<String, Object> response = new HashMap<>();

        if (!tracableOrNonTracable && (packageWeight > 0 && packageWeight <= 50) && swedenOrNot) {
            response.put("shippingCost", 18);
            response.put("message", "Delivery will take 1-2 business days");
        } else if (!tracableOrNonTracable && (packageWeight > 50 && packageWeight <= 100) && swedenOrNot) {
            response.put("shippingCost", 36);
            response.put("message", "Delivery will take 1-2 business days");
        } else if (!tracableOrNonTracable && (packageWeight > 100 && packageWeight <= 250) && swedenOrNot) {
            response.put("shippingCost", 54);
            response.put("message", "Delivery will take 1-2 business days");
        } else if (!tracableOrNonTracable && (packageWeight > 250 && packageWeight <= 500) && swedenOrNot) {
            response.put("shippingCost", 72);
            response.put("message", "Delivery will take 1-2 business days");
        } else if (!tracableOrNonTracable && (packageWeight > 500 && packageWeight <= 1000) && swedenOrNot) {
            response.put("shippingCost", 108);
            response.put("message", "Delivery will take 1-2 business days");
        } else if (!tracableOrNonTracable && (packageWeight > 1000 && packageWeight <= 2000) && swedenOrNot) {
            response.put("shippingCost", 126);
            response.put("message", "Delivery will take 1-2 business days");
        } else if (tracableOrNonTracable && (packageWeight > 0 && packageWeight <= 250) && swedenOrNot) {
            response.put("shippingCost", 58);
            response.put("message", "Tracable delivery will take 1-2 business days");
        } else if (tracableOrNonTracable && (packageWeight > 250 && packageWeight <= 500) && swedenOrNot) {
            response.put("shippingCost", 65);
            response.put("message", "Tracable delivery will take 1-2 business days");
        } else if (tracableOrNonTracable && (packageWeight > 500 && packageWeight <= 1000) && swedenOrNot) {
            response.put("shippingCost", 80);
            response.put("message", "Tracable delivery will take 1-2 business days");
        } else if (tracableOrNonTracable && (packageWeight > 1000 && packageWeight <= 2000) && swedenOrNot) {
            response.put("shippingCost", 118);
            response.put("message", "Tracable delivery will take 1-2 business days");
        } else if (!tracableOrNonTracable && (packageWeight > 0 && packageWeight <= 50) && !swedenOrNot) {
            response.put("shippingCost", 36);
            response.put("message", "Delivery will take 2-3 business days");
        } else if (!tracableOrNonTracable && (packageWeight > 50 && packageWeight <= 100) && !swedenOrNot) {
            response.put("shippingCost", 54);
            response.put("message", "Delivery will take 2-3 business days");
        } else if (!tracableOrNonTracable && (packageWeight > 100 && packageWeight <= 250) && !swedenOrNot) {
            response.put("shippingCost", 100);
            response.put("message", "Delivery will take 2-3 business days");
        } else if (!tracableOrNonTracable && (packageWeight > 250 && packageWeight <= 500) && !swedenOrNot) {
            response.put("shippingCost", 130);
            response.put("message", "Delivery will take 2-3 business days");
        } else if (!tracableOrNonTracable && (packageWeight > 500 && packageWeight <= 1000) && !swedenOrNot) {
            response.put("shippingCost", 190);
            response.put("message", "Delivery will take 2-3 business days");
        } else if (!tracableOrNonTracable && (packageWeight > 1000 && packageWeight <= 2000) && !swedenOrNot) {
            response.put("shippingCost", 230);
            response.put("message", "Delivery will take 2-3 business days");
        } else if (tracableOrNonTracable && (packageWeight > 0 && packageWeight <= 250) && !swedenOrNot) {
            response.put("shippingCost", 139);
            response.put("message", "Tracable delivery will take 2-3 business days");
        } else if (tracableOrNonTracable && (packageWeight > 250 && packageWeight <= 1000) && !swedenOrNot) {
            response.put("shippingCost", 199);
            response.put("message", "Tracable delivery will take 2-3 business days");
        } else if (tracableOrNonTracable && (packageWeight > 1000 && packageWeight <= 2000) && !swedenOrNot) {
            response.put("shippingCost", 280);
            response.put("message", "Tracable delivery will take 2-3 business days");
        } else {
            response.put("error", "Invalid package weight or parameters");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }*/

    public ResponseEntity<Map<String, Object>> calcualteShippingCost(boolean isSweden, boolean isEurope, boolean isTraceable, boolean isNonTraceable, double packageWeight) {

        Map<String, Object> response = new HashMap<>();
        double costMultiplier = 0.001; // Divide shipping cost by 10

        if (isNonTraceable && (packageWeight > 0 && packageWeight <= 50) && isSweden) {
            response.put("shippingCost", 18 * costMultiplier);
            response.put("message", "Delivery will take 1-2 business days");
        } else if (isNonTraceable && (packageWeight > 50 && packageWeight <= 100) && isSweden) {
            response.put("shippingCost", 36 * costMultiplier);
            response.put("message", "Delivery will take 1-2 business days");
        } else if (isNonTraceable && (packageWeight > 100 && packageWeight <= 250) && isSweden) {
            response.put("shippingCost", 54 * costMultiplier);
            response.put("message", "Delivery will take 1-2 business days");
        } else if (isNonTraceable && (packageWeight > 250 && packageWeight <= 500) && isSweden) {
            response.put("shippingCost", 72 * costMultiplier);
            response.put("message", "Delivery will take 1-2 business days");
        } else if (isNonTraceable && (packageWeight > 500 && packageWeight <= 1000) && isSweden) {
            response.put("shippingCost", 108 * costMultiplier);
            response.put("message", "Delivery will take 1-2 business days");
        } else if (isNonTraceable && (packageWeight > 1000 && packageWeight <= 2000) && isSweden) {
            response.put("shippingCost", 126 * costMultiplier);
            response.put("message", "Delivery will take 1-2 business days");
        } else if (isTraceable && (packageWeight > 0 && packageWeight <= 250) && isSweden) {
            response.put("shippingCost", 58 * costMultiplier);
            response.put("message", "Tracable delivery will take 1-2 business days");
        } else if (isTraceable && (packageWeight > 250 && packageWeight <= 500) && isSweden) {
            response.put("shippingCost", 65 * costMultiplier);
            response.put("message", "Tracable delivery will take 1-2 business days");
        } else if (isTraceable && (packageWeight > 500 && packageWeight <= 1000) && isSweden) {
            response.put("shippingCost", 80 * costMultiplier);
            response.put("message", "Tracable delivery will take 1-2 business days");
        } else if (isTraceable && (packageWeight > 1000 && packageWeight <= 2000) && isSweden) {
            response.put("shippingCost", 118 * costMultiplier);
            response.put("message", "Tracable delivery will take 1-2 business days");
        } else if (isNonTraceable && (packageWeight > 0 && packageWeight <= 50) && isEurope) {
            response.put("shippingCost", 36 * costMultiplier);
            response.put("message", "Delivery will take 2-3 business days");
        } else if (isNonTraceable && (packageWeight > 50 && packageWeight <= 100) && isEurope) {
            response.put("shippingCost", 54 * costMultiplier);
            response.put("message", "Delivery will take 2-3 business days");
        } else if (isNonTraceable && (packageWeight > 100 && packageWeight <= 250) && isEurope) {
            response.put("shippingCost", 100 * costMultiplier);
            response.put("message", "Delivery will take 2-3 business days");
        } else if (isNonTraceable && (packageWeight > 250 && packageWeight <= 500) && isEurope) {
            response.put("shippingCost", 130 * costMultiplier);
            response.put("message", "Delivery will take 2-3 business days");
        } else if (isNonTraceable && (packageWeight > 500 && packageWeight <= 1000) && isEurope) {
            response.put("shippingCost", 190 * costMultiplier);
            response.put("message", "Delivery will take 2-3 business days");
        } else if (isNonTraceable && (packageWeight > 1000 && packageWeight <= 2000) && isEurope) {
            response.put("shippingCost", 230 * costMultiplier);
            response.put("message", "Delivery will take 2-3 business days");
        } else if (isTraceable && (packageWeight > 0 && packageWeight <= 250) && isEurope) {
            response.put("shippingCost", 139 * costMultiplier);
            response.put("message", "Tracable delivery will take 2-3 business days");
        } else if (isTraceable && (packageWeight > 250 && packageWeight <= 1000) && isEurope) {
            response.put("shippingCost", 199 * costMultiplier);
            response.put("message", "Tracable delivery will take 2-3 business days");
        } else if (isTraceable && (packageWeight > 1000 && packageWeight <= 2000) && isEurope) {
            response.put("shippingCost", 280 * costMultiplier);
            response.put("message", "Tracable delivery will take 2-3 business days");
        } else {
            response.put("error", "Invalid package weight or parameters");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


 /*   private String generateOrFetchSessionToken(User savedGuestUser) {
        // Assuming you have a method to get the session token from the user or some other source
        String sessionToken = generateSessionTokenForUser(savedGuestUser); // Implement this method as needed

        // Check if a guest cart with this session token already exists
        GuestCart existingGuestCart = iRepoGuestCart.findBySessionToken(sessionToken);

        if (existingGuestCart != null) {
            // If a session token already exists, return it
            return existingGuestCart.getSessionToken();
        } else {
            // Generate a new session token using UUID
            String newSessionToken = UUID.randomUUID().toString();

            // Create a new GuestCart entity
            GuestCart newGuestCart = new GuestCart();
            newGuestCart.setSessionToken(newSessionToken);
            newGuestCart.setOrderPlaced(false); // Assuming this is the default state

            // Link the guest user with the guest cart
            newGuestCart.getUsers().add(savedGuestUser);  // Ensure the method exists to add user to guest cart

            // Save the GuestCart to associate the session token with the guest user
            iRepoGuestCart.save(newGuestCart);

            return newSessionToken;
        }
    }

    private String generateSessionTokenForUser(User savedGuestUser) {
        // Generate a unique session token using UUID
        return UUID.randomUUID().toString();
    }
    */
       

/*    private boolean checkPaymentStatus() {

    }*/
}


