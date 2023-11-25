package com.vikash.instBackend.controller;


import com.vikash.instBackend.dto.OrderEntityWithProductsDTO;
import com.vikash.instBackend.model.OrderEntity;
import com.vikash.instBackend.model.Product;
import com.vikash.instBackend.model.User;
import com.vikash.instBackend.model.enums.Type;
import com.vikash.instBackend.service.OrderEntityService;
import com.vikash.instBackend.service.ProductService;
import com.vikash.instBackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Validated
@RestController

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

    @PostMapping("/user/placingOrder/{userId}/productIds")
    public String placingOrder(@PathVariable Integer userId, @RequestBody List<Integer> productIds){
        return orderService.placingOrder(userId,productIds);
    }



    // get all products available

    @GetMapping("products")
    public List<Product> getAllProducts(){
        return productService.getAllProductss();
    }


    // move to admin
    @GetMapping("products/ids")
    public List<Product> getProductsByIds(@RequestBody List<Integer> ids){
        return  productService.getProductssById(ids);
    }


    // move to admin
    @GetMapping("product/{id}")
    public Optional<Product> getProductById(@PathVariable Integer id){

        return  productService.getProductsById(id);
    }


    @GetMapping("products/available")
    public List<Product> allAvailableProducts(){
        return productService.allAvailableProducts();
    }

    // get products by type
    @GetMapping("products/availableBy/type{type}")
    public List<Product> availableByType(@PathVariable Type type){

        return productService.availableByType(type);
    }

    // fetch order history with linked products
    @GetMapping("/user/{userId}/ordersWithProducts")
    public List<OrderEntityWithProductsDTO> getOrderHistoryByUserId(@PathVariable Integer userId) {
        List<OrderEntityWithProductsDTO> orderHistory = orderService.getOrderHistoryWithProducts(userId);
        return (orderHistory);
    }



    // get product by type and below price range
    @GetMapping("product/type{type}/belowPrice/{price}")
    public List<Product> availableByTypeAndLessThenEqualPrice( @PathVariable double price,@PathVariable Type type){

        return productService.availableAndLessThenEqualPrice(price,type);
    }

    // get products sort desc
    @GetMapping("product/sortBy/price/type/desc")
    public List<Product> sortByPriceDecsAndType(@PathVariable Type type){
        return productService.sortByPriceDecsAndType(type);

    }

    // get products sort asc

    @GetMapping("product/sortBy/price/type/asc")
    public List<Product> sortByPriceAscAndType(@PathVariable Type type){
        return productService.sortByPriceAscAndType(type);

    }


    // get product by name

    @GetMapping("product/productName/{name}")
    public  List<Product> getByProductName(@PathVariable String name ){
        return productService.getByProductName(name);
    }




}
