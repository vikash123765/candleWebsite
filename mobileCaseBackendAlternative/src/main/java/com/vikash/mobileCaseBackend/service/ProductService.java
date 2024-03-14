package com.vikash.mobileCaseBackend.service;


import com.vikash.mobileCaseBackend.model.OrderEntity;
import com.vikash.mobileCaseBackend.model.Product;
import com.vikash.mobileCaseBackend.model.User;
import com.vikash.mobileCaseBackend.model.enums.IncreasOrDeacrease;
import com.vikash.mobileCaseBackend.model.enums.Type;
import com.vikash.mobileCaseBackend.repo.IRepoOrder;
import com.vikash.mobileCaseBackend.repo.IRepoProduct;
import com.vikash.mobileCaseBackend.repo.IRepoUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        return repoProduct.findProductAvailableByProductType(type);

    }


    public List<Product> availableAndLessThenEqualPrice(Type type, double price) {
        return repoProduct.findProductAvailableByProductTypeAndProductPriceLessThanEqual(type, price);

    }

    public Optional<Product> getProductById(Integer id) {
        return repoProduct.findById(id);
    }


    public List<Product> getProductsById(List<Integer> ids) {
        return repoProduct.findAllById(ids);
    }


    public String updatePriceByType(String email, String tokenValue, IncreasOrDeacrease increasOrDeacrease, Type type, float discount) {
        if (authenticationService.authenticate(email, tokenValue)) {
            int polarity = (increasOrDeacrease == IncreasOrDeacrease.INCREASE) ? 1 : -1;

            for (Product product : availableByType(type)) {

                double originalPrice = product.getProductPrice();
                double priceAltering = originalPrice * (discount / 100) * polarity;
                ;
                double priceAfterAltering = originalPrice + priceAltering;
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

            int polarity = (increasOrDeacrease == IncreasOrDeacrease.INCREASE) ? 1 : -1;
            Product product = getProductById(id).orElseThrow();
            double originalPrice = product.getProductPrice();
            double priceAltering = originalPrice * (discount / 100) * polarity;
            ;
            double priceAfterAltering = originalPrice + priceAltering;
            String formattedPrice = String.format("%.2f", priceAfterAltering);
            formattedPrice = formattedPrice.replace(',', '.');
            product.setProductPrice(Double.parseDouble(formattedPrice));
            repoProduct.save(product);


            return "product with id: " + id + " price was updated";


        } else {
            return "Un Authenticated access!!!";
        }


    }

    public List<Product> sortByPriceDecsAndType(Type type) {
        return repoProduct.findProductAvailableByProductTypeOrderByProductPriceDesc(type);
    }

    public List<Product> sortByPriceAscAndType(Type type) {
        return repoProduct.findProductAvailableByProductTypeOrderByProductPriceAsc(type);
    }

    public String cancelOrRemoveOrderByOrderNr(String adminEmail, String tokenValue, Integer orderNr) {
        if (authenticationService.authenticate(adminEmail, tokenValue)) {
            // Find the actual order via the order number
            OrderEntity order = repoOrder.findByOrderNumber(orderNr);

            if (order != null) {
                // Unlink the order from the user
                User user = order.getUser();
                if (user != null) {
                    // Set the order reference of associated products to null
                    List<Product> products = repoProduct.findProductByOrders(order);
                    for (Product product : products) {
                        product.setOrders(null);
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

        return repoProduct.findProductAvailableByProductName(productName);
    }

    public String markProductAvailable(String adminEmail, String tokenValue, Integer productId) {
        if (authenticationService.authenticate(adminEmail, tokenValue)) {

            Product product = repoProduct.findById(productId).orElseThrow();

            product.setProductAvailable(true);

            repoProduct.save(product);

            return "product with id: " + productId + "was marked as available";


        } else {
            return "Un Authenticated access!!!";
        }


    }


    public String markProductsAvailable(String adminEmail, String tokenValue, List<Integer> productIds) {
        if (authenticationService.authenticate(adminEmail, tokenValue)) {

            List<Product> products = repoProduct.findAllById(productIds);

            for (Product product : products) {
                product.setProductAvailable(true);
                repoProduct.save(product);
            }
            //  repoProduct.saveAll(products);
            return "products with ids: " + productIds + "was marked as available";


        } else {
            return "Un Authenticated access!!!";
        }

    }

    public List<Product> allAvailableProducts() {
        return repoProduct.findByProductAvailable(true);
    }


    public String markProductUnAvailable(String adminEmail, String tokenValue, Integer productId) {
        if (authenticationService.authenticate(adminEmail, tokenValue)) {

            Product product = repoProduct.findById(productId).orElseThrow();

            product.setProductAvailable(false);

            repoProduct.save(product);

            return "product with id: " + productId + "was marked as unavailable";


        } else {
            return "Un Authenticated access!!!";
        }


    }

    public String markProductsUnAvailable(String adminEmail, String tokenValue, List<Integer> productIds) {
        if (authenticationService.authenticate(adminEmail, tokenValue)) {

            List<Product> products = repoProduct.findAllById(productIds);

            for (Product product : products) {
                product.setProductAvailable(true);
                repoProduct.save(product);
            }
            //  repoProduct.saveAll(products);
            return "products with ids: " + productIds + "was marked as unavailable";

        } else {
            return "Un Authenticated access!!!";
        }


    }


    public ResponseEntity<String> productAlterInfo(String adminEmail, String tokenValue, Integer productId, Product productFrontEnd) {
        if (authenticationService.authenticate(adminEmail, tokenValue)) {

            Product product = repoProduct.findById(productId).orElseThrow();
            if (product != null) {
                product.setProductId(productId);
                product.setProductAvailable(productFrontEnd.isProductAvailable());
                product.setProductDescription(productFrontEnd.getProductDescription());
                product.setProductName(productFrontEnd.getProductName());

                product.setProductPrice(productFrontEnd.getProductPrice());

                product.setProductType(productFrontEnd.getProductType());


                repoProduct.save(product);

                return new ResponseEntity<>("product with " + product.getProductId() + "id has been updated", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("product with " + product.getProductId() + "has not been  updated", HttpStatus.BAD_REQUEST);

            }
        }
                return  new ResponseEntity<>("Un Authenticated access!!!",HttpStatus.UNAUTHORIZED);
            }




}


