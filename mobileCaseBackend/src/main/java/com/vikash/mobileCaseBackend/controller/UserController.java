package com.vikash.mobileCaseBackend.controller;


import com.vikash.mobileCaseBackend.model.*;
import com.vikash.mobileCaseBackend.model.enums.Type;
import com.vikash.mobileCaseBackend.service.*;
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

    @Autowired
    CartService cartService;

    @Autowired
    GuestCartService guestCartService;


    // user sign up
    @PostMapping("user/signup")
    public String userSignUp(@Valid @RequestBody User newUser){
        return userService.userSignUp(newUser);
    }


    // user sign in

    // can not be using path variable insted shoyld be sent vi request body
    @PostMapping("user/signIn")
    public String UserSignIn(@RequestParam String email,@RequestParam String password ){
        return  userService.UserSignIn(email,password);
    }

    // user sign out

    @DeleteMapping("user/signOut")
    public String userSgnOut(@RequestParam String email,@RequestParam String token ){
        return userService.userSgnOut(email,token);
    }



    // actually placing order
    @PostMapping("/finalizeOrder")
    public String finalizeOrder(@RequestParam String email, @RequestParam String token){
        return orderService.finalizeOrder(email,token);
    }

    // add product to cart
    @PostMapping("add/product/toCart")
    public String addToCart(@RequestParam String email, @RequestParam String token, @RequestParam String productName ) {
        return cartService.addToCart(email,token,productName);

    }

   // add product to guest cart
    @PostMapping("add/products/guestCart")
    public String addToGuestCart( @RequestParam String productName) {
        return guestCartService.addToGuestCart(productName);

    }



    //  finalize order guest order

    @PostMapping("/finalizeGuestOrder/{guestCartId}")
    public String finalizeGuestOrder(@PathVariable Integer guestCartId, @RequestBody GuestOrderRequest guestOrderRequest) {
        return orderService.finalizeGuestOrder(guestCartId, guestOrderRequest);
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




    // logged in user order hsitpry
    @GetMapping("/user/orderHistory")
    public List<Map<String,Object>> getOrderHistoryByUserId(@RequestParam String email, @RequestParam String tokenValue) {
        return orderService.getOrderHistoryByUserEmail(email,tokenValue);
    }


    // get product by type and below price range
    @GetMapping("product/belowPrice/{price}")
    public List<Product> availableByTypeAndLessThenEqualPrice( @PathVariable double price){

        return productService.availableAndLessThenEqualPrice(price);
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
