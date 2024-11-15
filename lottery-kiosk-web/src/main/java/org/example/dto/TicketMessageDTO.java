package org.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.example.entities.LotteryTicket;

import java.io.Serializable;

@Data
public class TicketMessageDTO {

    private String requestType;
    private LotteryTicket ticket;

    public TicketMessageDTO(String requestType, LotteryTicket ticket) {
        this.requestType = requestType;
        this.ticket = ticket;
    }

}

