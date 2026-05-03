package com.municipal.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.springframework.stereotype.Service;

@Service
public class SmsService {

    // 🔥 Replace with your Fast2SMS API Key
    private final String API_KEY = "YOUR_FAST2SMS_API_KEY";

    public void sendSMS(String phone, String complaintId) {

        try {

            String message = "Your complaint has been registered. ID: " + complaintId;

            URL url = new URL("https://www.fast2sms.com/dev/bulkV2");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("authorization", API_KEY);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            conn.setDoOutput(true);

            String data = "message=" + URLEncoder.encode(message, "UTF-8")
                    + "&language=english"
                    + "&route=q"
                    + "&numbers=" + phone;

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();

            System.out.println("SMS API Response Code: " + responseCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
