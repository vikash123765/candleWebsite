package com.vikash.mobileCaseBackend.service;


import com.vikash.mobileCaseBackend.model.OrderEntity;
import com.vikash.mobileCaseBackend.model.Product;
import com.vikash.mobileCaseBackend.model.enums.IncreasOrDeacrease;
import com.vikash.mobileCaseBackend.model.enums.Type;
import com.vikash.mobileCaseBackend.repo.IRepoOrder;
import com.vikash.mobileCaseBackend.repo.IRepoProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    IRepoProduct repoProduct;
    @Autowired
    AuthService authenticationService;

    @Autowired
    IRepoOrder repoOrder;

    public String addProduct(String email, String tokenValue, Product productPost) {

        if (authenticationService.authenticate(email, tokenValue)) {
            repoProduct.save(productPost);
            return "product added";

        } else {
            return "Un Authenticated access!!!";
        }

    }


    public String addProducts(String email, String tokenValue, List<Product> newProducts) {
        if (authenticationService.authenticate(email, tokenValue)) {
            repoProduct.saveAll(newProducts);
            return "products added";

        } else {
            return "Un Authenticated access!!!";
        }

    }

    public String deleteProduct(String email, String tokenValue, Integer postId) {

        if (authenticationService.authenticate(email, tokenValue)) {
            repoProduct.deleteById(postId);
            return "product deleted";

        } else {
            return "Un Authenticated access!!!";
        }

    }

    public String removeAllProducts(String email, String tokenValue) {


        if (authenticationService.authenticate(email, tokenValue)) {
            repoProduct.deleteAll();
            return "all products deleted";

        } else {
            return "Un Authenticated access!!!";
        }

    }

    public String removeProductsByIds(String email, String tokenValue, List<Integer> ids) {

        if (authenticationService.authenticate(email, tokenValue)) {
            repoProduct.deleteAllById(ids);
            return "products with given id  deleted";

        } else {
            return "Un Authenticated access!!!";
        }

    }


    public List<Product> allAvailableProducts() {
        return repoProduct.findByProductAvailable(true);
    }

    public List<Product> availableByType(Type type) {
        return repoProduct.findByProductAvailableAndProductType(true,type);

    }

    public List<Product> getByProductName(String name) {
        return  repoProduct.findByProductName(name);
    }

    public List<Product> availableAndLessThenEqualPrice(double price,Type type) {
      return repoProduct.findByProductPriceLessThanEqualAndProductType(price, type);

    }

    public Optional<Product> getProductById(Integer id) {
        return repoProduct.findById(id);
    }

    public List<Product> getAllProductss() {
        return repoProduct.findAll();
    }

    public List<Product> getProductssById(List<Integer> ids) {
        return repoProduct.findAllById(ids);
    }


    public String updatePriceByType(String email, String tokenValue,IncreasOrDeacrease increasOrDeacrease, Type type, float discount) {
        if (authenticationService.authenticate(email, tokenValue)) {
            int polarity =( increasOrDeacrease == IncreasOrDeacrease.INCREASE) ? 1:-1;

            for (Product product : availableByType(type)){

                double originalPrice = product.getProductPrice();
                double priceAltering = originalPrice * (discount / 100) * polarity;;
                double priceAfterAltering  = originalPrice + priceAltering;
                String formattedPrice = String.format("%.2f", priceAfterAltering);
                formattedPrice = formattedPrice.replace(',', '.');
                product.setProductPrice(Double.parseDouble(formattedPrice));
                //product.setProductPrice(priceAfterAltering);
                repoProduct.save(product);

            }
            return "price updated";


        } else {
            return "Un Authenticated access!!!";
        }

    }

    public String updatePriceById(String email, String tokenValue, Integer id, IncreasOrDeacrease increasOrDeacrease, float discount) {
        if (authenticationService.authenticate(email, tokenValue)) {

                int polarity =( increasOrDeacrease == IncreasOrDeacrease.INCREASE) ? 1:-1;
                Product product= getProductById(id).orElseThrow() ;
                double originalPrice = product.getProductPrice();
                double priceAltering = originalPrice * (discount / 100) * polarity;;
                double priceAfterAltering  = originalPrice + priceAltering;
                String formattedPrice = String.format("%.2f", priceAfterAltering);
                formattedPrice = formattedPrice.replace(',', '.');
                product.setProductPrice(Double.parseDouble(formattedPrice));
            repoProduct.save(product);


            return"product with id: " + id + " price was updated";


        } else {
            return "Un Authenticated access!!!";
        }


    }

    public List<Product> sortByPriceDecsAndType(Type type) {
        return repoProduct.findByProductTypeOrderByProductPriceDesc(type);
    }

    public List<Product> sortByPriceAscAndType(Type type) {
        return repoProduct.findByProductTypeOrderByProductPriceAsc(type);
    }

    public String removeOrderByOrderNr(String adminEmail, String tokenValue, Integer orderNr) {
        if (authenticationService.authenticate(adminEmail, tokenValue)) {
            OrderEntity order =  repoOrder.findByOrderNumber(orderNr);

            // first we need to remove product that is connected with order nr
            List<Product> products = repoProduct.findProductByOrderEntity(order);
            repoProduct.deleteAll(products);

            repoOrder.delete(order);


           return"order with id: " + orderNr + " was removed";


        } else {
            return "Un Authenticated access!!!";
        }

    }
}




