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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public String UserSignIn(@RequestParam String adminEmail, @RequestParam String adminPassword ){
        return  adminService.adminSignIn(adminEmail,adminPassword);
    }

    @DeleteMapping("admin/signOut")
    public String adminSgnOut(@RequestParam String adminEmail,@RequestParam String token ){
        return adminService.adminSgnOut(adminEmail,token);
    }

    //get  products based on ids


    @GetMapping("products/ids")
    public List<Product> getProductsByIds(@RequestBody List<Integer> ids){
        return  productService.getProductssById(ids);
    }


    // get products by id
    @GetMapping("product/{id}")
    public Optional<Product> getProductById(@PathVariable Integer id){

        return  productService.getProductById(id);
    }

    // post
    @PostMapping("product")
    public String addProduct(@RequestParam String adminEmail, @RequestParam String tokenValue, @RequestBody Product productPost){
        return productService.addProduct(adminEmail,tokenValue,productPost);
    }
    @PostMapping("products")
    public String addProducts(@RequestParam String adminEmail, @RequestParam String tokenValue, @RequestBody List<Product> newProducts){
        return productService.addProducts(adminEmail,tokenValue,newProducts);
    }

    // delete
    @DeleteMapping("product/{productId}")
    public String deletePost(@RequestParam String adminEmail, @RequestParam String tokenValue, @PathVariable Integer postId){
        return productService.deleteProduct(adminEmail,tokenValue,postId);
    }
    @DeleteMapping("products")
    public String removeAllProducts(@RequestParam String adminEmail, @RequestParam String tokenValue){
        return productService.removeAllProducts(adminEmail,tokenValue);
    }

    @DeleteMapping("products/ids")
    public String removeProductsByIds(@RequestParam String adminEmail, @RequestParam String tokenValue,@RequestBody List<Integer> ids){
        return productService.removeProductsByIds(adminEmail,tokenValue,ids);
    }

    // cancel an order
    @DeleteMapping("order/{orderNr}")
    public String cancelOrderByOrderNr(@RequestParam String adminEmail, @RequestParam String tokenValue,@PathVariable  Integer orderNr){
        return productService.cancelOrderByOrderNr(adminEmail,tokenValue,orderNr);
    }

    //mark order as sent

    @PutMapping("order/sent/{orderNr}/{trackingId}")
    String markOrderAsSent (@RequestParam String adminEmail, @RequestParam String tokenValue,@PathVariable Integer orderNr,  @RequestParam(required = false) Integer trackingId){


        return orderService.markOrderAsSent(adminEmail,tokenValue,orderNr,trackingId);
    }

    // mark order as delivered
    @PutMapping("order/delivered/{orderNr}")
    String markOrderAsDelivered (@RequestParam String adminEmail, @RequestParam String tokenValue,@PathVariable Integer orderNr){


        return orderService.markOrderAsDelivered(adminEmail,tokenValue,orderNr);
    }


    // marlk product as avaiable

    @PostMapping("markAvailable/product/{productId}")
    String markProductAvailable(@PathVariable Integer productId){
        return productService.markProductAvailable(productId);
    }


    @PostMapping("markAvailable/productIds")
    String markProductAvailable(@RequestBody List<Integer> productIds){
        return productService.markProductsAvailable(productIds);
    }


    // put
    // increase/decrease price by category
    @PutMapping("products/type/{type}/increaseOrDeacrease/{increasOrDeacrease}/percentage{discount}")
    public String updatePriceByType(@RequestParam String email, @RequestParam String tokenValue,@PathVariable IncreasOrDeacrease increasOrDeacrease,@PathVariable Type  type,@PathVariable float discount){
        return productService.updatePriceByType(email,tokenValue,increasOrDeacrease,type,discount);
    }

    // incraese/deacrese price by id
    @PutMapping("products/id/{id}/increaseOrDeacrease/{increasOrDeacrease}/percentage/{discount}")
    public String updatePriceById(@RequestParam String email, @RequestParam String tokenValue,@PathVariable IncreasOrDeacrease increasOrDeacrease,@PathVariable Integer id,@PathVariable float discount){
        return productService.updatePriceById(email,tokenValue,id,increasOrDeacrease,discount);
    }



}
