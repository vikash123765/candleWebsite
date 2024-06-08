package com.vikash.mobileCaseBackend.service;


import com.vikash.mobileCaseBackend.model.*;
import com.vikash.mobileCaseBackend.repo.IRepoCart;
import com.vikash.mobileCaseBackend.repo.IRepoCartItem;
import com.vikash.mobileCaseBackend.repo.IRepoProduct;
import com.vikash.mobileCaseBackend.repo.IRepoUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    IRepoCartItem iRepoCartItem;

    @Autowired
    IRepoCart repoCart;

    @Autowired
    AuthService authService;

    @Autowired
    IRepoProduct repoProduct;

    @Autowired
    IRepoUser repoUser;







        public String addToCart(String email, String token, String productName) {
            if (authService.authenticate(email, token)) {
                // Find the user by email
                User actualUser = repoUser.findByUserEmail(email);

                // Fetch the products based on the provided product name and availability
                List<Product> products = repoProduct.findByProductAvailableAndProductName(true, productName);

                if (products.isEmpty()) {
                    return "Product not found or is not available: " + productName;
                }

                // Assuming you want to add the first product found (you might want to refine this logic)
                Product productToAdd = products.get(0);

                // Check if the user already has a cart
                Cart userCart = actualUser.getCart();

                if (userCart == null) {
                    // If the user doesn't have a cart, create a new one
                    userCart = new Cart();
                    userCart.setUser(actualUser);
                }

                // Check if the product is already in the cart
                List<CartItem> cartItems = userCart.getCartItems();

                Optional<CartItem> existingCartItem = cartItems.stream()
                        .filter(item -> item.getProduct().equals(productToAdd))
                        .findFirst();

                if (existingCartItem.isPresent()) {
                    // If the product is already in the cart, update the quantity or take appropriate action
                    existingCartItem.get().setQuantity(existingCartItem.get().getQuantity() + 1);
                } else {
                    // If the product is not in the cart, create a new cart item
                    CartItem cartItem = new CartItem();
                    cartItem.setProduct(productToAdd);
                    cartItem.setQuantity(1);
                    cartItem.setCart(userCart);
                    cartItems.add(cartItem);


                }

                // Save the user's cart (this cascades to cart items)
                repoCart.save(userCart);



                return "Product added to the cart successfully.";
            } else {
                return "Unauthenticated access!!!";
            }
        }


    public Cart getCartByUser(User user) {
        return repoCart.findByUser(user);

    }

    public void resetCart(User user) {
        Cart cart = repoCart.findByUser(user);

        if (cart != null) {
            // Delete associated cart items
            List<CartItem> cartItems = cart.getCartItems();
            iRepoCartItem.deleteAll(cartItems);

            // Clear the cart items list
            cartItems.clear();

            // Save the updated cart
            repoCart.save(cart);
        }
    }
}
