package org.example.dto;

import org.example.entities.LotteryTicket;

import java.io.Serializable;

public class TicketMessageDTO implements Serializable {
    private String requestType;
    private LotteryTicket ticket;

    public TicketMessageDTO(String requestType, LotteryTicket ticket) {
        this.requestType = requestType;
        this.ticket = ticket;
    }

    // Getters and setters
}

