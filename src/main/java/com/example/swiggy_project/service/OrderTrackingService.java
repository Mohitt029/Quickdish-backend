package com.example.swiggy_project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderTrackingService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendOrderStatusUpdate(String orderId, String status) {
        messagingTemplate.convertAndSend("/topic/order/track/" + orderId, status);
    }
}