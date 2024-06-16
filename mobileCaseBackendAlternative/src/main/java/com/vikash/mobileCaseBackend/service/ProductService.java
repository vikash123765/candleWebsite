package com.vikash.mobileCaseBackend.service;


import com.vikash.mobileCaseBackend.model.OrderEntity;
import com.vikash.mobileCaseBackend.model.Product;
import com.vikash.mobileCaseBackend.model.ProductOrderSnapshot;
import com.vikash.mobileCaseBackend.model.User;
import com.vikash.mobileCaseBackend.model.enums.IncreasOrDeacrease;
import com.vikash.mobileCaseBackend.model.enums.Type;
import com.vikash.mobileCaseBackend.repo.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

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
    @Autowired
    AdminService adminService; // Assuming you have an AdminService for marking product availability
    @Autowired
    IProductOrderSnapshot repoProductOrderSnapshot;

    @Autowired
    iRepoProductOrder productOrderRepository;


    private LocalDateTime lastExecutionTime = LocalDateTime.now();

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




    public List<Product> availableAndLessThenEqualPrice(Type type,double price) {
        return repoProduct.findProductAvailableByProductTypeAndProductPriceLessThanEqual(type,price);

    }

    public Optional<Product> getProductById(Integer id) {
        return repoProduct.findById(id);
    }


    public List<Product> getProductsById(List<Integer> ids) {
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


            return"product with idd: " + id + " price was updated";


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

    public String markProductAvailable(String adminEmail, String tokenValue,Integer productId) {
        if (authenticationService.authenticate(adminEmail, tokenValue)) {

            Product product = repoProduct.findById(productId).orElseThrow();

            product.setProductAvailable(true);

            repoProduct.save(product);

            return "product with id: " + productId + "was marked as available";




        } else {
            return "Un Authenticated access!!!";
        }


    }





    public String markProductsAvailable(String adminEmail, String tokenValue,List<Integer> productIds) {
        if (authenticationService.authenticate(adminEmail, tokenValue)) {

            List<Product> products = repoProduct.findAllById(productIds);

            for (Product product: products){
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
        return  repoProduct.findByProductAvailable(true);
    }



    public String markProductUnAvailable(String adminEmail, String tokenValue,Integer productId) {
        if (authenticationService.authenticate(adminEmail, tokenValue)) {

            Product product = repoProduct.findById(productId).orElseThrow();

            product.setProductAvailable(false);

            repoProduct.save(product);

            return "product with id: " + productId + "was marked as unavailable";


        } else {
            return "Un Authenticated access!!!";
        }



    }

    public String markProductsUnAvailable(String adminEmail, String tokenValue,List<Integer> productIds) {
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








    @Scheduled(fixedRate = 300) // Run every 5 minutes (300000 ms)
    @Transactional
    public void updateProductStock() {
        LocalDateTime currentExecutionTime = LocalDateTime.now();

        // Fetch the most recent snapshot
        List<ProductOrderSnapshot> lastSnapshots = repoProductOrderSnapshot.findTopSnapshot();
        Map<Integer, Long> lastSnapshotMap = new HashMap<>();
        if (lastSnapshots != null && !lastSnapshots.isEmpty()) {
            for (ProductOrderSnapshot snapshot : lastSnapshots) {
                lastSnapshotMap.put(snapshot.getProductId(), snapshot.getQuantity());
            }
        }

        // Fetch current product orders
        List<Object[]> currentProductOrders = productOrderRepository.findProductOrderQuantities();

        // Create snapshots for current product orders
        List<ProductOrderSnapshot> newSnapshots = new ArrayList<>();
        boolean hasChanges = false; // Flag to track if there are any changes

        for (Object[] result : currentProductOrders) {
            Integer productId = (Integer) result[0];
            Long quantity = (Long) result[1];

            // Check if the quantity has changed compared to the last snapshot
            Long lastQuantity = lastSnapshotMap.getOrDefault(productId, 0L);
            Long changeInQuantity = quantity - lastQuantity;

            if (changeInQuantity != 0) {
                hasChanges = true; // Set the flag if there are changes
                // Update stock based on the change in quantity
                Product product = repoProduct.findById(productId).orElse(null);
                if (product != null) {
                    int updatedStock = product.getStock() - changeInQuantity.intValue();
                    if (updatedStock < 0) {
                        updatedStock = 0;
                    }
                    product.setStock(updatedStock);
                    if (updatedStock <= 0) {
                        product.setProductAvailable(false);
                    }
                    repoProduct.save(product);
                }
            }

            // Save the new snapshot
            ProductOrderSnapshot snapshot = new ProductOrderSnapshot();
            snapshot.setProductId(productId);
            snapshot.setQuantity(quantity);
            snapshot.setSnapshotTime(currentExecutionTime);
            newSnapshots.add(snapshot);
        }

        if (hasChanges) {
            repoProductOrderSnapshot.saveAll(newSnapshots);
        }

        // Update the last execution time to the current time
        lastExecutionTime = currentExecutionTime;
    }

    public ResponseEntity<String> numberOfAvailableProducts(Integer productId, Integer count) {

        Product product = repoProduct.findById(productId).orElseThrow();
        Integer currentStock = product.getStock();

        if(count > currentStock ){
            return new ResponseEntity<>("sorry we only have this many at the moment"+currentStock, HttpStatus.BAD_REQUEST);
        }
        else {
            return new ResponseEntity<>("sucessfull we have this many on hand", HttpStatus.OK);
        }
    }
}




