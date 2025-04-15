package com.mail.mail_demoapp.controller;

import com.mail.mail_demoapp.dto.EmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/email")
public class EmailController {

    private final JavaMailSender mailSender;

    // Inject the configured sender email from application.properties
    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest emailRequest) {
        try {
            // Create a SimpleMailMessage object to represent the email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            // Set the recipients, subject, and content from the request
            message.setTo(emailRequest.getTo().toArray(new String[0])); // Set recipients as an array
            message.setSubject(emailRequest.getSubject());
            message.setText(emailRequest.getText());
            mailSender.send(message);
            return ResponseEntity.ok("Email sent successfully");

        } catch (Exception e) {
            // Handle any errors that occur during sending the email
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email: " + e.getMessage());
        }
    }
    @PostMapping(value="/send/html" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> sendHtmlEmail(@RequestPart("email") EmailRequest emailRequest,@RequestPart(value = "file", required = false) MultipartFile multipartFile) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);  // "true" for multipart

            helper.setFrom(fromEmail);  // Sender email
            helper.setTo(emailRequest.getTo().toArray(new String[0]));  // Convert List to Array
            helper.setSubject(emailRequest.getSubject());
            if (multipartFile != null && !multipartFile.isEmpty()) {
                helper.addAttachment(multipartFile.getOriginalFilename(), multipartFile);
            }
            // HTML content for the email with inline styles
            String htmlContent = "<html>" +
                    "<head>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }" +
                    "h1 { color: #333; text-align: center; }" +
                    "p { font-size: 16px; color: #555; line-height: 1.5; text-align: left; padding: 20px; }" +
                    ".email-container { background-color: #ffffff; border-radius: 10px; max-width: 600px; margin: 30px auto; padding: 20px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); }" +
                    ".footer { text-align: center; font-size: 12px; color: #888; margin-top: 20px; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='email-container'>" +
                    "<h1>" + "You are shortlisted" + "</h1>" +
                    "<p>" + emailRequest.getText() + "</p>" +
                    "<div class='footer'>Mention your availability</div>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);  // "true" to indicate HTML content

            mailSender.send(message);
            return ResponseEntity.ok("HTML email sent successfully");

        } catch (MessagingException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send HTML email: " + e.getMessage());
        }
    }

}
