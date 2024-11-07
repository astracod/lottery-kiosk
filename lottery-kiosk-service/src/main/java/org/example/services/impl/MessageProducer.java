package org.example.services.impl;

import org.example.entities.LotteryTicket;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public MessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(String requestType, LotteryTicket ticket) {
        // Отправка сообщения с префиксом типа запроса
        String routingKey = "ticketQueue"; // Убедитесь, что это правильное название вашей очереди
        rabbitTemplate.convertAndSend(routingKey, requestType + ":" + ticket);
    }
}

