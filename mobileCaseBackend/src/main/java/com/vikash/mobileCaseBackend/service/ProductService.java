package com.vikash.mobileCaseBackend.service;


import com.vikash.mobileCaseBackend.model.OrderEntity;
import com.vikash.mobileCaseBackend.model.Product;
import com.vikash.mobileCaseBackend.model.User;
import com.vikash.mobileCaseBackend.model.enums.IncreasOrDeacrease;
import com.vikash.mobileCaseBackend.model.enums.Type;
import com.vikash.mobileCaseBackend.repo.IRepoOrder;
import com.vikash.mobileCaseBackend.repo.IRepoProduct;
import com.vikash.mobileCaseBackend.repo.IRepoUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
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

    @Autowired
    IRepoUser iRepoUser;

    private final EntityManager entityManager;

    public ProductService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


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





    public List<Product> availableByType(Type type) {
        return repoProduct.findFirstProductAvailableByProductType(type);

    }




    public List<Product> availableAndLessThenEqualPrice(double price) {
      return repoProduct.findFirstProductAvailableByProductPriceLessThanEqual(price);

    }

    public Optional<Product> getProductById(Integer id) {
        return repoProduct.findById(id);
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
        return repoProduct.findFirstProductAvailableByProductTypeOrderByProductPriceDesc(type);
    }

    public List<Product> sortByPriceAscAndType(Type type) {
        return repoProduct.findFirstProductAvailableByProductTypeOrderByProductPriceAsc(type);
    }

    public String cancelOrderByOrderNr(String adminEmail, String tokenValue, Integer orderNr) {
        if (authenticationService.authenticate(adminEmail, tokenValue)) {
            // Find the actual order via the order number
            OrderEntity order = repoOrder.findByOrderNumber(orderNr);

            if (order != null) {
                // Unlink the order from the user
                User user = order.getUser();
                if (user != null) {
                    // Set the order reference of associated products to null
                    List<Product> products = repoProduct.findProductByOrderEntity(order);
                    for (Product product : products) {
                        product.setOrderEntity(null);
                        product.setProductAvailable(true);
                    }

                    order.setUser(null);
                    iRepoUser.save(user); // Save the user to update the changes in the relationship
                }

                // Save changes

                repoOrder.delete(order);

                return "Order with id: " + orderNr + " was removed.";
            } else {
                return "Order not found.";
            }
        } else {
            return "Unauthenticated access!!!";
        }
    }
    public List<Product> findProductByName(String productName) {
        return  repoProduct.findFirstProductAvailableByProductName(productName);
    }

    public String markProductAvailable(Integer productId) {
        Product product = repoProduct.findById(productId).orElseThrow();

        product.setProductAvailable(true);

        repoProduct.save(product);

        return "product with id: " + productId + "was marked as available";

    }

    public String markProductsAvailable(List<Integer> productIds) {
        List<Product> products = repoProduct.findAllById(productIds);

         for (Product product: products){
             product.setProductAvailable(true);
             repoProduct.save(product);
         }
        //  repoProduct.saveAll(products);
         return "products with ids: " + productIds + "was marked as available";
    }

    public List<Product> allAvailableProducts() {
        return  repoProduct.findFirstProductAvailableByProductAvailable(true);
    }
}




