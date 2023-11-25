package com.vikash.instBackend.controller;

import com.vikash.instBackend.model.Admin;
import com.vikash.instBackend.model.Product;
import com.vikash.instBackend.model.enums.IncreasOrDeacrease;
import com.vikash.instBackend.model.enums.Type;
import com.vikash.instBackend.service.AdminService;
import com.vikash.instBackend.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Validated
@RestController
public class AdminController {

    @Autowired
    ProductService productService;

    @Autowired
    AdminService adminService;





    // admin sign up

    @PostMapping("admin/signup")
    public String adminSignUp(@Valid @RequestBody Admin newAdmin){
        return adminService.adminSignUp(newAdmin);
    }

    // admin sign in

    @PostMapping("admin/signIn/{adminEmail}/{adminPassword}")
    public String UserSignIn(@PathVariable String adminEmail, @PathVariable String adminPassword ){
        return  adminService.adminSignIn(adminEmail,adminPassword);
    }

    @DeleteMapping("admin/signOut")
    public String adminSgnOut(@RequestParam String adminEmail,@RequestParam String token ){
        return adminService.adminSgnOut(adminEmail,token);
    }

    //get

    // get all orders for a pertucular userid
  /*  @GetMapping("orders/user{userId}")
    public String getOrdersPerUser(@PathVariable Integer userId){
        return
    }*/

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

    // need api that marks product as sent it sends mail to user




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
