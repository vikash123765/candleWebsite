package com.vikash.mobileCaseBackend.controller;


import com.vikash.mobileCaseBackend.model.GuestOrderRequestAndProducts;
import com.vikash.mobileCaseBackend.model.Product;
import com.vikash.mobileCaseBackend.model.User;
import com.vikash.mobileCaseBackend.model.enums.Type;
import com.vikash.mobileCaseBackend.service.OrderEntityService;
import com.vikash.mobileCaseBackend.service.ProductService;
import com.vikash.mobileCaseBackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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


    // user sign up
    @PostMapping("user/signup")
    public String userSignUp(@Valid @RequestBody User newUser){
        return userService.userSignUp(newUser);
    }


    // user sign in

    @PostMapping("user/signIn/{email}/{password}")
    public String UserSignIn(@PathVariable String email,@PathVariable String password ){
        return  userService.UserSignIn(email,password);
    }

    // user sign out

    @DeleteMapping("user/signOut")
    public String userSgnOut(@RequestParam String email,@RequestParam String token ){
        return userService.userSgnOut(email,token);
    }

    // needs to add functionality that order info is sent via mail to the the one who has placed the order and admin gmail. '
    // registered user placing order
    @PostMapping("user/placingOrder/productIds")
    public String placingOrder(@RequestParam String email, @RequestParam String token
            , @RequestBody List<Integer> productIds){
        return orderService.placingOrder(email,token,productIds);
    }

    // needs to add functionality that order info is sent via mail to the the one who has placed the order and admin gmail. '
    // guest user placing order
    @PostMapping("guest/placingOrder")
    public String guestCheckout(@RequestBody GuestOrderRequestAndProducts requestAndProducts) {
            return orderService.processGuestOrder(requestAndProducts);
    }



    // get all products

    @GetMapping("products")
    public List<Product> getAllProducts(){
        return productService.getAllProductss();
    }


    // get all products available
    @GetMapping("products/available")
    public List<Product> allAvailableProducts(){
        return productService.allAvailableProducts();
    }

    // get products by type
    @GetMapping("products/availableBy/type{type}")
    public List<Product> availableByType(@PathVariable Type type){

        return productService.availableByType(type);
    }


    // get product by name

    @GetMapping("product/productName/{name}")
    public  List<Product> getByProductName(@PathVariable String name ){
        return productService.getByProductName(name);
    }




    // need to add checks that only admin and user that is logged is trycing to acess his own orders and not others.
    // same logic for deleting a comment in instaBAckend
    @GetMapping("/user/orderHistory/{userId}")
    public List<Map<String,Object>> getOrderHistoryByUserId(@RequestParam String email, @RequestParam String tokenValue) {
        return orderService.getOrderHistoryByUserId(email,tokenValue);
    }


    // get product by type and below price range
    @GetMapping("product/type{type}/belowPrice/{price}")
    public List<Product> availableByTypeAndLessThenEqualPrice( @PathVariable double price,@PathVariable Type type){

        return productService.availableAndLessThenEqualPrice(price,type);
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
