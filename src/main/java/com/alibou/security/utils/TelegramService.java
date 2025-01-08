package com.alibou.security.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TelegramService {
    private final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
    private final String BOT_TOKEN = "8143055131:AAFOOSIAlwSftJgvCrrqiO2Xo3PYP1e979c"; // Replace with your bot token
    private final long CHAT_ID = -4688303158L; // Define chatId as a constant

    public String sendMessageToGroup(String message) {
        String url = TELEGRAM_API_URL + BOT_TOKEN + "/sendMessage";
        String payload = String.format("{\"chat_id\": %d, \"text\": \"%s\"}", CHAT_ID, message);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.postForObject(url, entity, String.class);
        } catch (Exception e) {
            return "Error sending message: " + e.getMessage();
        }
    }
}
