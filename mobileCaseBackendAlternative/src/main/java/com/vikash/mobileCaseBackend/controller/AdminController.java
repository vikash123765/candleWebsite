package com.vikash.mobileCaseBackend.controller;

import com.vikash.mobileCaseBackend.model.Admin;
import com.vikash.mobileCaseBackend.model.Product;
import com.vikash.mobileCaseBackend.model.enums.IncreasOrDeacrease;
import com.vikash.mobileCaseBackend.model.enums.Type;
import com.vikash.mobileCaseBackend.service.AdminService;
import com.vikash.mobileCaseBackend.service.OrderEntityService;
import com.vikash.mobileCaseBackend.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Validated
@RestController
@CrossOrigin(origins = "*") // Allow requests from any origin
public class AdminController {

    @Autowired
    ProductService productService;

    @Autowired
    AdminService adminService;

    @Autowired
    OrderEntityService orderService;





    // admin sign up

    @PostMapping("admin/signup")
    public String adminSignUp(@Valid @RequestBody Admin newAdmin){
        return adminService.adminSignUp(newAdmin);
    }

    // admin sign in

    @PostMapping("admin/signIn")
    public ResponseEntity<String> adminSignIn(@RequestHeader("email") String adminEmail, @RequestHeader("password") String adminPassword ){

        return adminService.adminSignIn(adminEmail, adminPassword);
    }



    @DeleteMapping("admin/signOut")
    public ResponseEntity<String> adminSgnOut( @RequestHeader("x-auth-token") String token){

        return adminService.adminSgnOut(token);
    }

    //get  products based on ids


    @PostMapping("products/ids")
    public List<Product> getProductsByIds(@RequestBody List<Integer> ids){
        return  adminService.getProductsById(ids);
    }


    // get products by id
    @GetMapping("product/{id}")
    public Optional<Product> getProductById(@PathVariable Integer id){

        return  productService.getProductById(id);
    }

    // post
    @PostMapping("product")
    public String addProduct(@RequestHeader("email") String adminEmail, @RequestHeader("x-auth-token") String tokenValue,  @RequestBody Product productPost){
        return productService.addProduct(adminEmail,tokenValue,productPost);
    }
    @PostMapping("products")
    public String addProducts(@RequestHeader("email") String adminEmail, @RequestHeader("x-auth-token") String tokenValue,  @RequestBody List<Product> newProducts){
        return productService.addProducts(adminEmail,tokenValue,newProducts);
    }

    @GetMapping("isAdminLoggedIn")
    public ResponseEntity<Boolean> isAdminLoggedIn(@RequestHeader("adminToken") String adminToken){
        return adminService.isAdminLoggedIn(adminToken);
    }

    // delete
    @DeleteMapping("product/{productId}")
    public String deletePost(@RequestHeader("email") String adminEmail, @RequestHeader("x-auth-token") String tokenValue,  @PathVariable Integer postId){
        return productService.deleteProduct(adminEmail,tokenValue,postId);
    }
    @DeleteMapping("products")
    public String removeAllProducts(@RequestHeader("email") String adminEmail, @RequestHeader("x-auth-token") String tokenValue){
        return productService.removeAllProducts(adminEmail,tokenValue);
    }

    @DeleteMapping("products/ids")
    public String removeProductsByIds(@RequestHeader("email") String adminEmail, @RequestHeader("x-auth-token") String tokenValue, @RequestBody List<Integer> ids){
        return productService.removeProductsByIds(adminEmail,tokenValue,ids);
    }

    // remove/cancel an order
    @DeleteMapping("order/{orderNr}")
    public String cancelOrRemoveOrderByOrderNr(@RequestHeader("email") String adminEmail, @RequestHeader("x-auth-token") String tokenValue, @PathVariable  Integer orderNr){
        return productService.cancelOrRemoveOrderByOrderNr(adminEmail,tokenValue,orderNr);
    }

    //mark order as sent

    @PutMapping("order/sent/{orderNr}/{trackingId}")
    public ResponseEntity<String> markOrderAsSent (@RequestHeader("email") String adminEmail, @RequestHeader("x-auth-token") String tokenValue, @PathVariable Integer orderNr, @PathVariable(required = false) Integer trackingId){


        return orderService.markOrderAsSent(adminEmail,tokenValue,orderNr,trackingId);
    }

    // mark order as delivered
    @PutMapping("order/delivered/{orderNr}")
    public ResponseEntity<String> markOrderAsDelivered (@RequestHeader("email") String adminEmail, @RequestHeader("x-auth-token") String tokenValue, @PathVariable Integer orderNr){


        return orderService.markOrderAsDelivered(adminEmail,tokenValue,orderNr);
    }


    // alter product
    @PostMapping("product/alterInfo")
    public ResponseEntity<String> productAlterInfo(@RequestHeader("email") String adminEmail, @RequestHeader("x-auth-token")  String tokenValue,@RequestHeader Integer productId,@RequestBody Product productFrontEnd ){
        return productService.productAlterInfo(adminEmail,tokenValue,productId,productFrontEnd);
    }


    // marlk product as avaiable

    @PostMapping("markAvailable/product/{productId}")
    public String markProductAvailable(@RequestHeader("email") String adminEmail, @RequestHeader("x-auth-token") String tokenValue, @PathVariable Integer productId){
        return productService.markProductAvailable(adminEmail,tokenValue,productId);
    }

    // marlk product as unavaiable

    @PostMapping("markUnAvailable/product/{productId}")
    public String markProductUnAvailable(@RequestHeader("email") String adminEmail, @RequestHeader("x-auth-token") String tokenValue, @PathVariable Integer productId){
        return productService.markProductUnAvailable(adminEmail,tokenValue,productId);
    }


    @PostMapping("markAvailable/productIds")
    public String markProductAvailable(@RequestHeader("email") String adminEmail, @RequestHeader("x-auth-token") String tokenValue,@RequestBody List<Integer> productIds){
        return productService.markProductsAvailable(adminEmail,tokenValue,productIds);
    }


    @PostMapping("markUnAvailable/productIds")
    public String markProductsUnAvailable(@RequestHeader("email") String adminEmail, @RequestHeader("x-auth-token") String tokenValue, @RequestBody List<Integer> productIds){
        return productService.markProductsUnAvailable(adminEmail,tokenValue,productIds);
    }


    // put
    // increase/decrease price by category
    @PutMapping("products/type/{type}/increaseOrDeacrease/{increasOrDeacrease}/percentage{discount}")
    public String updatePriceByType(@RequestHeader("email") String adminEmail, @RequestHeader("x-auth-token") String tokenValue, @PathVariable IncreasOrDeacrease increasOrDeacrease,@PathVariable Type  type,@PathVariable float discount){
        return productService.updatePriceByType(adminEmail,tokenValue,increasOrDeacrease,type,discount);
    }

    // incraese/deacrese price by id
    @PutMapping("products/id/{id}/increaseOrDeacrease/{increasOrDeacrease}/percentage/{discount}")
    public String updatePriceById(@RequestHeader("email") String adminEmail, @RequestHeader("x-auth-token") String tokenValue, @PathVariable IncreasOrDeacrease increasOrDeacrease,@PathVariable Integer id,@PathVariable float discount){
        return productService.updatePriceById(adminEmail,tokenValue,id,increasOrDeacrease,discount);
    }



}
