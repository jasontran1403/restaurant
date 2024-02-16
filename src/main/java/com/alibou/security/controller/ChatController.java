package com.alibou.security.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.alibou.security.dto.ChatMessage;
import com.alibou.security.service.OrderService;

@Controller
public class ChatController {
    @Autowired
	OrderService orderService;
	
    @MessageMapping("/register")
	@SendTo("/topic/public")
	public ChatMessage register(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
		headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
		return chatMessage;
	}

	@MessageMapping("/send")
	@SendTo("/topic/public")
	public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
		return chatMessage;
	}
}
