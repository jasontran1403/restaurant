package com.alibou.security.service;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.alibou.security.dto.Message;
import com.alibou.security.entity.Order;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RealtimeService {
	private final SimpMessagingTemplate messagingTemplate;
	
	@MessageMapping("/message")
    @SendTo("/chatroom/public")
    public Message receiveMessage(@Payload Message message){
        return message;
    }

	public void sendNewOrderNotification(Order order) {
		// Xử lý và lưu đơn hàng vào cơ sở dữ liệu

		// Sau khi xử lý xong, gửi thông báo đến tất cả máy khách đang theo dõi kênh
		// '/topic/orders'
		messagingTemplate.convertAndSend("/topic/orders", order);
	}
}
