package com.vikash.mobileCaseBackend.service;
import com.vikash.mobileCaseBackend.model.*;
import com.vikash.mobileCaseBackend.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class GuestCartService {

    @Autowired
    IRepoGuestCart iRepoGuestCart;

    @Autowired
    ProductService productService;

    @Autowired
    IRepoGuestCartItem iRepoGuestCartItem;

    @Autowired
    IRepoProduct repoProduct;

    private static final int RESERVATION_EXPIRATION_MINUTES = 30;

    public String addToGuestCart(String productName) {
        // Fetch the products based on the provided product name and availability
        List<Product> products = repoProduct.findByProductAvailableAndProductName(true, productName);

        if (products.isEmpty()) {
            return "Product not found or is not available: " + productName;
        }

        // Assuming you want to add the first product found
        Product productToAdd = products.get(0);

        // Check if the guest cart exists
        GuestCart guestCart = iRepoGuestCart.findById(1).orElse(null);
        if (guestCart == null) {
            // If the guest cart doesn't exist, create a new one
            guestCart = new GuestCart();
            iRepoGuestCart.save(guestCart);
        }

        // Check if the product is already in the guest cart
        List<GuestCartItem> guestCartItems = guestCart.getGuestCartItems();
        Optional<GuestCartItem> existingCartItem = guestCartItems.stream()
                .filter(item -> item.getProduct().equals(productToAdd))
                .findFirst();

        if (existingCartItem.isPresent()) {
            // If the product is already in the guest cart, update the quantity or take appropriate action
            existingCartItem.get().setQuantity(existingCartItem.get().getQuantity() + 1);
        } else {
            // If the product is not in the guest cart, create a new guest cart item
            GuestCartItem guestCartItem = new GuestCartItem();
            guestCartItem.setProduct(productToAdd);
            guestCartItem.setQuantity(1); // Assuming a default quantity of 1
            guestCartItem.setGuestCart(guestCart);
            guestCartItems.add(guestCartItem);

            // Mark the product as unavailable when added to the guest cart
            productToAdd.setProductAvailable(false);


            // Release expired reservations after adding the product
            releaseExpiredReservations(guestCart);
        }

        // Save the guest cart (this cascades to guest cart items)
        iRepoGuestCart.save(guestCart);


        return "Product added to the guest cart successfully.";
    }




    public String removeFromGuestCart(Integer guestCartItemId) {
        // Fetch the guest cart item
        Optional<GuestCartItem> guestCartItemOptional = iRepoGuestCartItem.findById(guestCartItemId);

        if (guestCartItemOptional.isPresent()) {
            GuestCartItem guestCartItem = guestCartItemOptional.get();

            // Mark the product as available again
            guestCartItem.getProduct().setProductAvailable(true);

            // Remove the guest cart item
            iRepoGuestCartItem.deleteById(guestCartItemId);

            return "Product removed from guest cart successfully.";
        } else {
            return "Guest cart item not found.";
        }
    }

    private void releaseExpiredReservations(GuestCart guestCart) {
        LocalDateTime currentTime = LocalDateTime.now();

        Iterator<GuestCartItem> iterator = guestCart.getGuestCartItems().iterator();
        while (iterator.hasNext()) {
            GuestCartItem guestCartItem = iterator.next();

            LocalDateTime reservationTime = guestCartItem.getProduct().getReservationTime();

            // Check if reservationTime is not null before calculating the duration
            if (reservationTime != null) {
                if (Duration.between(reservationTime, currentTime).toMinutes() > RESERVATION_EXPIRATION_MINUTES) {
                    // Mark the product as available again
                    guestCartItem.getProduct().setProductAvailable(true);

                    // Remove the expired item from the guest cart
                    iterator.remove();
                }
            } else {
                // Handle the case where reservationTime is null (optional)
                // You may want to log a message or take other appropriate action
            }
        }
    }
}
