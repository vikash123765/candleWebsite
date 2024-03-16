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
        final String username = "vikash.kosaraju1234@gmail.com";
        final String password = "wffu jvky tjow ozji"; // Use your App Password if 2FA is enabled

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com"); // Trust the Gmail server

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



            double totalPrice = 0.0;
            // Include order details in the email body
            String orderDetails = "Order Number: " + order.getOrderNumber() + "\n\n";
            orderDetails += " order  Details:\n";
            orderDetails += "  - Name: " + order.getUser().getUserName() + "\n";
            orderDetails += "  - Email: " + order.getUser().getUserEmail() + "\n";
            orderDetails += "  - Shipping Address: " + order.getUser().getAddress() + "\n";
            orderDetails += "  - Phone Number: " + order.getUser().getPhoneNumber() + "\n\n";
            orderDetails += "Products:\n";


            List<Product> productsForOrder = getProductsForOrder(order);
            for (Product product : productsForOrder) {
                totalPrice += product.getProductPrice();
                orderDetails += "  - " + product.getProductName() + ": " + product.getProductPrice() + " kr";
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
        return productRepository.findProductByOrders(order);
    }

}


