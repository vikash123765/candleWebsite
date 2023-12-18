package com.vikash.mobileCaseBackend.service.EmailUtility;

import com.vikash.mobileCaseBackend.model.OrderEntity;
import com.vikash.mobileCaseBackend.model.Product;
import com.vikash.mobileCaseBackend.repo.IRepoOrder;
import com.vikash.mobileCaseBackend.repo.IRepoProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
@Service
public class SendMailOrderInfo {
    @Autowired
    private
    IRepoOrder orderEntityRepository; // Assuming you have a Spring Data JPA repository for OrderEntity

    @Autowired
    private
    IRepoProduct productRepository; // Assuming you have a Spring Data JPA repository for Product

    public void sendEmail(String to, String subject, String body, OrderEntity order) {
        final String username = "vikash.kosaraju1234@gmail.com"; // your Gmail email address
        final String password = "llhb pfzy jdfa krpo"; // your Gmail email password

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            // Include order details in the email body
            String orderDetails = "Order Number: " + order.getOrderNumber() + "\n\n";
            orderDetails += "Products:\n";

            // Calculate total price manually
            double totalPrice = 0.0;

            // fetch the actual products for order
            List<Product> productsForOrder = getProductsForOrder(order);

            // iterate over the products and add one by one
            for (Product product : productsForOrder) {
                totalPrice += product.getProductPrice();
            }

            // Include order details in the email body
            for (Product product : productsForOrder) {
                orderDetails += "  - " + product.getProductName() + ": " + product.getProductPrice() + " units\n";
            }

           orderDetails += "\nTotal Price: $" + totalPrice;

            message.setText(body + "\n\n" + orderDetails);

            Transport.send(message);

            System.out.println("Email sent to: " + to);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Product> getProductsForOrder(OrderEntity order) {
        List<Product> products = new ArrayList<>();

        List<Product> allProducts = productRepository.findAll();

        for (Product product : allProducts) {
            if (order.equals(product.getOrderEntity())) {
                products.add(product);
            }
        }

        return products;
    }
}


