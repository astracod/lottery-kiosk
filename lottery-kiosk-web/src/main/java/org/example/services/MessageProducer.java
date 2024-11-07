package org.example.services;

import org.example.dto.TicketMessageDTO;
import org.example.entities.LotteryTicket;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageProducer {
    private final RabbitTemplate rabbitTemplate;

    public MessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(LotteryTicket ticket) {
        rabbitTemplate.convertAndSend("ticketQueue", ticket);
    }

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend("ticketQueue", message);
    }

    public void sendMessage(String requestType, LotteryTicket ticket) {
        TicketMessageDTO message = new TicketMessageDTO(requestType, ticket);
        rabbitTemplate.convertAndSend("ticketQueue", message);
    }
}

