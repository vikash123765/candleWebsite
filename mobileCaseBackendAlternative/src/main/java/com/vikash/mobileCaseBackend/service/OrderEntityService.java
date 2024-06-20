
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
    iRepoProductOrder repoProductOrder;


    public List<Map<String, Object>> getOrderHistoryByUserEmail(String token) {


        // figure out the actual token with tokenvalue
        AuthenticationToken actualToken = authRepo.findByTokenValue(token);

        // Check if actualToken is not null
        if (actualToken != null) {
            // figure out the email of the user

            // we get the user here
            User user = actualToken.getUser();

            List<OrderEntity> orders = repoOrder.findOrdersByUser(user);

            if (orders == null || orders.isEmpty()) {
                return Collections.singletonList(Collections.singletonMap("message", user.getUserName() + " does not have any orders"));
            }


            List<Map<String, Object>> orderList = new ArrayList<>();

            for (OrderEntity order : orders) {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("orderId", order.getOrderNumber());
                orderMap.put("userName", order.getUser().getUserName());
                orderMap.put("sent", order.isMarkAsSent());
                orderMap.put("orderCreated", order.getCreationTimeStamp());
                orderMap.put("delivered", order.isMarkAsDelivered());
                orderMap.put("trackingId", order.getTrackingNumber());

                // Fetch products and quantities associated with the order
                List<Object[]> productOrderQuantities = repoProductOrder.findProductOrderQuantities(order.getOrderNumber());
                List<Map<String, Object>> productDetails = new ArrayList<>();
                double totalOrderCost = 0.0;

                for (Object[] result : productOrderQuantities) {
                    Map<String, Object> productMap = new HashMap<>();
                    productMap.put("productId", result[0]);
                    productMap.put("orderId", result[1]);
                    productMap.put("quantity", result[2]);

                    // Assuming you have a method to fetch product details by productId
                    Product product = repoProduct.findById((Integer) result[0]).orElse(null);
                    if (product != null) {
                        productMap.put("productName", product.getProductName());
                        productMap.put("productType", product.getProductType());
                        productMap.put("productPrice", product.getProductPrice());

                        totalOrderCost += product.getProductPrice() * (Long) result[2];

                    }

                    productDetails.add(productMap);
                }

                orderMap.put("products", productDetails);
                orderMap.put("totalCost", totalOrderCost);
                orderList.add(orderMap);
            }

            return orderList;
        }


        // If token is not valid or user not found, return empty list or handle accordingly
        return Collections.emptyList();
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


    public ResponseEntity<String> finalizeOrder(String token, String jsonPayload) {
        if (authenticationService.authenticateUserLoggedIn(token)) {
            try {
                AuthenticationToken tokenObj = authRepo.findByTokenValue(token);
                User user = tokenObj.getUser();

                System.out.println("User authenticated successfully: " + user.getUserName());

                // Fetch initial product quantities
                Map<Integer, Integer> initialQuantities = new HashMap<>();
                List<Object[]> initialResults = repoProductOrder.findProductOrderQuantitiesGroupedByProductId();
                System.out.println("Initial results size: " + initialResults.size());
                for (Object[] result : initialResults) {
                    Integer productId = (Integer) result[0];
                    Long quantity = (Long) result[1]; // Assuming quantity is Long (COUNT result)
                    initialQuantities.put(productId, quantity != null ? quantity.intValue() : 0);
                    System.out.println("Initial - ProductId: " + productId + ", Quantity: " + quantity);
                }
                // Parse the JSON payload into a List of product IDs
                List<Integer> productIds;
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    productIds = objectMapper.readValue(jsonPayload, new TypeReference<List<Integer>>() {});
                    System.out.println("Parsed product IDs from JSON payload: " + productIds);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error parsing JSON payload", e);
                }

                // Create and populate an OrderEntity
                OrderEntity order = new OrderEntity();
                order.setUser(user);
                order.setCreationTimeStamp(LocalDateTime.now());

                // Save the order to the database
                repoOrder.save(order);
                System.out.println("Order saved successfully: " + order.getOrderNumber());

                // Retrieve products by productIds and create associations with the order
                for (Integer productId : productIds) {
                    Product product = repoProduct.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Product not found for ID: " + productId));

                    // Add the product to the order's products list
                    order.getProducts().add(product);
                    System.out.println("Product added to order - ProductId: " + productId + ", ProductName: " + product.getProductName());
                }

                // Save the order with associated products
                repoOrder.save(order);
                System.out.println("Order updated with associated products");

                // Update the user's orders
                user.getOrders().add(order);
                repoUser.save(user);
                System.out.println("User updated with new order");

                // Fetch final product quantities after associating products
                Map<Integer, Integer> finalQuantities = new HashMap<>();
                List<Object[]> finalResults = repoProductOrder.findProductOrderQuantitiesGroupedByProductId();
                System.out.println("Final results size: " + finalResults.size());
                for (Object[] result : finalResults) {
                    Integer productId = (Integer) result[0];
                    Long quantity = (Long) result[1]; // Assuming quantity is Long (COUNT result)
                    finalQuantities.put(productId, quantity != null ? quantity.intValue() : 0);
                    System.out.println("Final - ProductId: " + productId + ", Quantity: " + quantity);
                }

                // Update product stock based on differences
                for (Integer productId : finalQuantities.keySet()) {
                    Integer finalQuantity = finalQuantities.getOrDefault(productId, 0);
                    Integer initialQuantity = initialQuantities.getOrDefault(productId, 0);
                    Integer changeInQuantity = finalQuantity - initialQuantity;

                    if (changeInQuantity > 0) {
                        Product product = repoProduct.findById(productId).orElse(null);
                        if (product != null) {
                            int updatedStock = product.getStock() - changeInQuantity;
                            if (updatedStock < 0) {
                                updatedStock = 0;
                            }
                            product.setStock(updatedStock);
                            if (updatedStock <= 0) {
                                product.setProductAvailable(false);
                            }
                            repoProduct.save(product);
                            System.out.println("Product stock updated - ProductId: " + productId + ", Updated Stock: " + updatedStock);
                        }
                    }
                }

                // Send email notifications
                String userSubject = "Order Placed";
                String userBody = "Your order has been placed. Thank you for shopping with us!";
                sendMailOrderInfo.sendEmail(user.getUserEmail(), userSubject, userBody, order);

                String adminEmail = "vts.cases.ad@gmail.com";
                String adminSubject = "New Order Placed";
                String adminBody = "A new order has been placed. Order Number: " + order.getOrderNumber();
                sendMailOrderInfo.sendEmail(adminEmail, adminSubject, adminBody, order);

                return new ResponseEntity<>("Order finalized successfully!", HttpStatus.OK);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>("Error processing order: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Unauthorized access", HttpStatus.UNAUTHORIZED);
        }
    }


    public String finalizeGuestOrder(GuestOrderRequest guestOrderRequest, String jsonPayload) {
        String passedEmail = guestOrderRequest.getEmail();

        // Check if the user with the provided email already exists
        User user = repoUser.findByUserEmail(passedEmail);

        if (user == null) {
            // User doesn't exist, proceed with creating a new guest user
            user = new User();
            user.setUserName(guestOrderRequest.getUserName());
            user.setUserEmail(guestOrderRequest.getEmail());
            user.setAddress(guestOrderRequest.getShippingAddress());
            user.setPhoneNumber(guestOrderRequest.getPhoneNumber());

            // Save the new guest user to the database
            user = repoUser.save(user);
        } else {
            // Update the existing user's details
            user.setAddress(guestOrderRequest.getShippingAddress());
            user.setPhoneNumber(guestOrderRequest.getPhoneNumber());
            repoUser.save(user);
        }

        // Fetch initial product quantities
        Map<Integer, Integer> initialQuantities = new HashMap<>();
        List<Object[]> initialResults = repoProductOrder.findProductOrderQuantitiesGroupedByProductId();
        System.out.println("Initial results size: " + initialResults.size());
        for (Object[] result : initialResults) {
            Integer productId = (Integer) result[0];
            Long quantity = (Long) result[1]; // Assuming quantity is Long (COUNT result)
            initialQuantities.put(productId, quantity != null ? quantity.intValue() : 0);
            System.out.println("Initial - ProductId: " + productId + ", Quantity: " + quantity);
        }

        // Create and populate a GuestOrderEntity
        OrderEntity guestOrder = new OrderEntity();
        guestOrder.setMarkAsSent(false); // Set default values for other order-related fields
        guestOrder.setMarkAsDelivered(false);
        guestOrder.setCreationTimeStamp(LocalDateTime.now());

        // Link the guest order with the user
        guestOrder.setUser(user);

        // Save the order to the database
        repoOrder.save(guestOrder);

        // Parse the JSON payload into a List of product IDs
        List<Integer> productIds;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            productIds = objectMapper.readValue(jsonPayload, new TypeReference<List<Integer>>() {});
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
        user.getOrders().add(guestOrder);
        repoUser.save(user);

        // Fetch final product quantities after associating products
        Map<Integer, Integer> finalQuantities = new HashMap<>();
        List<Object[]> finalResults = repoProductOrder.findProductOrderQuantitiesGroupedByProductId();
        System.out.println("Final results size: " + finalResults.size());
        for (Object[] result : finalResults) {
            Integer productId = (Integer) result[0];
            Long quantity = (Long) result[1]; // Assuming quantity is Long (COUNT result)
            finalQuantities.put(productId, quantity != null ? quantity.intValue() : 0);
            System.out.println("Final - ProductId: " + productId + ", Quantity: " + quantity);
        }

        // Update product stock based on differences
        for (Integer productId : finalQuantities.keySet()) {
            Integer finalQuantity = finalQuantities.getOrDefault(productId, 0);
            Integer initialQuantity = initialQuantities.getOrDefault(productId, 0);
            Integer changeInQuantity = finalQuantity - initialQuantity;

            if (changeInQuantity > 0) {
                Product product = repoProduct.findById(productId).orElse(null);
                if (product != null) {
                    int updatedStock = product.getStock() - changeInQuantity;
                    if (updatedStock < 0) {
                        updatedStock = 0;
                    }
                    product.setStock(updatedStock);
                    if (updatedStock <= 0) {
                        product.setProductAvailable(false);
                    }
                    repoProduct.save(product);
                    System.out.println("Product stock updated - ProductId: " + productId + ", Updated Stock: " + updatedStock);
                }
            }
        }

        // Send email notifications
        String userSubject = "Guest Order Placed";
        String userBody = "Your guest order has been placed. Thank you for shopping with us!";
        sendMailOrderInfo.sendEmail(user.getUserEmail(), userSubject, userBody, guestOrder);

        String adminEmail = "vts.cases.ad@gmail.com"; // Replace with your actual admin email
        String adminSubject = "New Guest Order Placed";
        String adminBody = "A new guest order has been placed. Order Number: " + guestOrder.getOrderNumber();
        sendMailOrderInfo.sendEmail(adminEmail, adminSubject, adminBody, guestOrder);

        return "Guest order finalized successfully!";
    }

/*

    public ResponseEntity<Map<String, Object>>calcualteShippingCost(boolean isSweden, boolean isEurope, boolean isTraceable, boolean isNonTraceable, double packageWeight) {
        Map<String, Object> response = new HashMap<>();

        if ((isSweden || isEurope) && (isTraceable || isNonTraceable)) {
            if (isSweden) {
                if (isNonTraceable) {
                    if (packageWeight > 0 && packageWeight <= 50) {
                        response.put("shippingCost", 18);
                    } else if (packageWeight > 50 && packageWeight <= 100) {
                        response.put("shippingCost", 36);
                    } else if (packageWeight > 100 && packageWeight <= 250) {
                        response.put("shippingCost", 54);
                    } else if (packageWeight > 250 && packageWeight <= 500) {
                        response.put("shippingCost", 72);
                    } else if (packageWeight > 500 && packageWeight <= 1000) {
                        response.put("shippingCost", 108);
                    } else if (packageWeight > 1000 && packageWeight <= 2000) {
                        response.put("shippingCost", 126);
                    }
                    response.put("message", "Delivery with Postnord will take 1-2 business days");
                } else if (isTraceable) {
                    if (packageWeight > 0 && packageWeight <= 250) {
                        response.put("shippingCost", 58);
                    } else if (packageWeight > 250 && packageWeight <= 500) {
                        response.put("shippingCost", 65);
                    } else if (packageWeight > 500 && packageWeight <= 1000) {
                        response.put("shippingCost", 80);
                    } else if (packageWeight > 1000 && packageWeight <= 2000) {
                        response.put("shippingCost", 118);
                    }
                    response.put("message", "Delivery with Postnord will take 1-2 business days");
                }
            } else if (isEurope) {
                if (isNonTraceable) {
                    if (packageWeight > 0 && packageWeight <= 50) {
                        response.put("shippingCost", 36);
                    } else if (packageWeight > 50 && packageWeight <= 100) {
                        response.put("shippingCost", 54);
                    } else if (packageWeight > 100 && packageWeight <= 250) {
                        response.put("shippingCost", 100);
                    } else if (packageWeight > 250 && packageWeight <= 500) {
                        response.put("shippingCost", 130);
                    } else if (packageWeight > 500 && packageWeight <= 1000) {
                        response.put("shippingCost", 190);
                    } else if (packageWeight > 1000 && packageWeight <= 2000) {
                        response.put("shippingCost", 230);
                    }
                    response.put("message", "Delivery with Postnord will take 2-3 business days");
                } else if (isTraceable) {
                    if (packageWeight > 0 && packageWeight <= 250) {
                        response.put("shippingCost", 139);
                    } else if (packageWeight > 250 && packageWeight <= 1000) {
                        response.put("shippingCost", 199);
                    } else if (packageWeight > 1000 && packageWeight <= 2000) {
                        response.put("shippingCost", 280);
                    }
                    response.put("message", "Delivery with Postnord will take 2-3 business days");
                }
            }
        } else {
            response.put("error", "Invalid parameters");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

*/





    public ResponseEntity<Map<String, Object>>calcualteShippingCost (boolean isSweden, boolean isEurope, boolean isTraceable, boolean isNonTraceable, double packageWeight) {

        Map<String, Object> response = new HashMap<>();
        double costMultiplier = 0.001; // Divide shipping cost by 10 for testing purposes

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
            response.put("shippingCost", 58 * costMultiplier); // Corrected this value
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
            response.put("shippingCost", 199 * costMultiplier); // Adjusted this value to fit the weight
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


