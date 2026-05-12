package com.municipal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // ✅ Get sender email from application.properties (ENV)
    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendComplaintEmail(String to, String complaintId) {

        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String trackLink = "https://spring-boot-minor-project.onrender.com/track.html?id=" + complaintId;

            String htmlContent
                    = "<h3>✅ Complaint Registered Successfully</h3>"
                    + "<p>Dear Citizen,</p>"
                    + "<p>Your complaint has been successfully registered.</p>"
                    + "<p><b>Complaint ID:</b> " + complaintId + "</p>"
                    + "<br>"
                    + "<p>Click below to track your complaint:</p>"
                    + "<br>"
                    + "<a href='" + trackLink + "' "
                    + "style='padding:10px 15px; background:#28a745; color:white; text-decoration:none; border-radius:5px;'>"
                    + "🔍 Track Here</a>"
                    + "<br><br>"
                    + "<p>Or copy this link:</p>"
                    + "<p>" + trackLink + "</p>"
                    + "<br>"
                    + "<p>Thank you.</p>"
                    + "<p><i>Municipal Complaint Management System, TML</i></p>";

            helper.setTo(to);
            helper.setSubject("Complaint Registered Successfully");
            helper.setText(htmlContent, true);

            // ✅ VERY IMPORTANT (missing in your code)
            helper.setFrom(fromEmail, "Municipal Complaint System");

            mailSender.send(message);

            System.out.println("✅ Email sent successfully to: " + to);

        } catch (Exception e) {
            System.out.println("❌ Email sending failed");
            e.printStackTrace();
        }
    }
}
