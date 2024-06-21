package com.vikash.mobileCaseBackend.service.EmailUtility;

import com.vikash.mobileCaseBackend.model.OrderEntity;
import com.vikash.mobileCaseBackend.model.Product;
import com.vikash.mobileCaseBackend.repo.IRepoOrder;
import com.vikash.mobileCaseBackend.repo.IRepoProduct;
import com.vikash.mobileCaseBackend.repo.iRepoProductOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;

@Service
public class SendMailOrderInfo {
    @Autowired

    IRepoOrder orderEntityRepository; // Assuming you have a Spring Data JPA repository for OrderEntity

    @Autowired

    IRepoProduct productRepository; // Assuming you have a Spring Data JPA repository for Product

    @Autowired
    iRepoProductOrder iRepoProductOrder;

    public void sendEmail(String to, String subject, String body, OrderEntity order) {
        final String username = "vtscases.buissness@gmail.com";
        final String password = "smnw prmr ouei zpgx"; // Use your App Password if 2FA is enabled



        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com"); // Trust the Gmail server

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            // Prepare email body
            StringBuilder orderDetails = new StringBuilder();
            orderDetails.append("Order Number: ").append(order.getOrderNumber()).append("\n\n");
            orderDetails.append("Order Details:\n");
            orderDetails.append("  - Name: ").append(order.getUser().getUserName()).append("\n");
            orderDetails.append("  - Email: ").append(order.getUser().getUserEmail()).append("\n");
            orderDetails.append("  - Shipping Address: ").append(order.getUser().getAddress()).append("\n");
            orderDetails.append("  - Phone Number: ").append(order.getUser().getPhoneNumber()).append("\n\n");
            orderDetails.append("Products:\n");

            // Fetch products and their quantities for the order
            List<Object[]> productQuantities = iRepoProductOrder.findProductOrderQuantities(order.getOrderNumber());

            double totalPrice = 0.0;

            // Append each product to the email body
            for (Object[] result : productQuantities) {
                int productId = (int) result[0];
                int orderId = (int) result[1];
                long quantity = (long) result[2]; // Assuming quantity is returned as long

                // Assuming you have a method to fetch product details based on productId
                Product product = getProductDetails(productId);

                if (product != null) {
                    double productPrice = product.getProductPrice();

                    totalPrice += productPrice * quantity;
                    orderDetails.append("  - ").append(product.getProductName()).append(": ")
                            .append(productPrice).append(" SEK").append(" x ").append(quantity).append("\n");
                }
            }

            orderDetails.append("\nTotal Price: SEK ").append(totalPrice).append(" + Shipping charges");
            message.setText(body + "\n\n" + orderDetails.toString());

            Transport.send(message);
            System.out.println("Email sent to: " + to);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private Product getProductDetails(int productId) {
        return productRepository.findById(productId).orElse(null);

    }
}

