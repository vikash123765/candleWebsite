package com.vikash.mobileCaseBackend.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.vikash.mobileCaseBackend.model.*;
import com.vikash.mobileCaseBackend.model.enums.Type;
import com.vikash.mobileCaseBackend.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@Validated
@RestController

@CrossOrigin(origins = "*") // Allow requests from any origin
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    ProductService productService;

    @Autowired
    OrderEntityService orderService;

    @Autowired
    CartService cartService;

    @Autowired
    GuestCartService guestCartService;


    // user sign up
    @PostMapping("user/signUp")
    public ResponseEntity<Map<String, String>> userSignUp(@Valid @RequestBody User newUser) throws JsonProcessingException {
        return userService.userSignUp(newUser);
    }


    // user sign in


    @PostMapping("user/signIn")
    public ResponseEntity<String> UserSignIn(@RequestHeader("email") String email, @RequestHeader("password") String password) {
        return userService.userSignIn(email, password);
    }
    // user sign out

    @DeleteMapping("user/signOut")
    public ResponseEntity<String> userSgnOut(@RequestHeader("x-auth-token") String token) {
        return userService.userSgnOut(token);
    }


    @GetMapping("user/loggedIn/info")
    public ResponseEntity<?> loggedInInfo(@RequestHeader String token) {
        return userService.userSingedInInfo(token);

    }


    // actually placing order
 /*   @PostMapping("/finalizeOrder")
    public String finalizeOrder(@RequestHeader("email") String email, @RequestHeader("x-auth-token") String token) {
        // Validate the token and process the order
        return orderService.finalizeOrder(email, token);
    }
*/

    @PutMapping("/user/alterInfo")
    public ResponseEntity<User> alterUserInfo(@RequestHeader("token") String token, @RequestBody User user) throws NoSuchAlgorithmException {
        return userService.alterUserInfo(token, user);
    }

    @PostMapping("/finalizeOrder")
    public ResponseEntity<String> finalizeOrder(@RequestHeader("token") String token, @RequestBody String jsonPayload, boolean checkPyment) {
        // Validate the token and process the order
        return orderService.finalizeOrder(token, jsonPayload);
    }

    @PostMapping("user/loggedIn/customerService")
    public ResponseEntity<String> customerServiceContactLoggedInUser(@RequestHeader("subject") String subject, @RequestHeader("token") String token, @RequestBody String message) {
        System.out.println(subject);
        System.out.println(token);
        System.out.println(message);
        return userService.customerServiceContactLoggedInUser(subject, token, message);
    }


    @PostMapping("guest/customerService/message")
    public ResponseEntity<String> guestCustomerService(@RequestHeader String subject,@RequestHeader String senderEmail, @RequestBody String message) {
        return userService.guestCustomerService(subject,senderEmail,message);
    }
/*
    // add product to cart
    @PostMapping("add/product/toCart")
    public String addToCart(@RequestHeader("email") String email, @RequestHeader("x-auth-token") String token, @RequestParam String productName) {
        return cartService.addToCart(email,token,productName);

    }*/
/*

   // add product to guest cart
    @PostMapping("add/products/guestCart")
    public String addToGuestCart( @RequestParam String productName) {
        return guestCartService.addToGuestCart(productName);

    }
*/


    //  finalize order guest order


    @GetMapping("calculate-shipping-rates/{isSweden}/{isEurope}/{isTraceable}/{isNonTraceable}/{packageWeight}")
    public ResponseEntity<Map<String, Object>> calculateShippingCost(
            @PathVariable boolean isSweden,
            @PathVariable boolean isEurope,
            @PathVariable boolean isTraceable,
            @PathVariable boolean isNonTraceable,
            @PathVariable double packageWeight) {
        return orderService.calcualteShippingCost(isSweden, isEurope, isTraceable, isNonTraceable, packageWeight);
    }





    @PostMapping("/finalizeGuestOrder")
    public String finalizeGuestOrder(@RequestBody GuestOrderRequestWrapper requestWrapper) {
        return orderService.finalizeGuestOrder(requestWrapper.getGuestOrderRequest(), requestWrapper.getJsonPayload());
    }




    // get all products available
    @GetMapping("products/available")
    public List<Product> allAvailableProducts(){
        return productService.allAvailableProducts();
    }

    // get products avaiable  by type
    @GetMapping("products/availableBy/type{type}")
    public List<Product> availableByType(@PathVariable Type type){

        return productService.availableByType(type);
    }


    // get product  availble by name

    @GetMapping("product/productName/{name}")
    public  List<Product> getByProductName(@PathVariable String name ){
        return productService.findProductByName(name);
    }


    // alter password
    @PostMapping("change/password")

    public ResponseEntity<String> changePassword(@RequestHeader String token,@RequestHeader String oldPassword,@RequestHeader String newPassword) throws NoSuchAlgorithmException {
        return userService.changePassword(token,oldPassword,newPassword);
    }



    // logged in user order hsitpry

    @GetMapping("/user/orderHistory")
    public List<Map<String,Object>> getOrderHistoryByUserId(@RequestHeader String token) {
        return orderService.getOrderHistoryByUserEmail(token);
    }



    // get product by type and below price range

    @GetMapping("product/belowPrice/{price}/type{type}")
    public List<Product> availableByTypeAndLessThenEqualPrice( @PathVariable double price, @PathVariable Type type){

        return productService.availableAndLessThenEqualPrice(type,price);
    }

    // get products sort desc
    @GetMapping("product/sortBy/price/{type}/desc")
    public List<Product> sortByPriceDecsAndType(@PathVariable Type type){
        return productService.sortByPriceDecsAndType(type);

    }

    // get products sort asc

    @GetMapping("product/sortBy/price/{type}/asc")
    public List<Product> sortByPriceAscAndType(@PathVariable Type type){
        return productService.sortByPriceAscAndType(type);

    }



}
