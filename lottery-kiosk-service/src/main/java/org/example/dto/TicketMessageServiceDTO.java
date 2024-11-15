package org.example.dto;

import lombok.Data;
import org.example.entities.LotteryTicket;

@Data
public class TicketMessageServiceDTO {
    private String requestType;
    private LotteryTicket ticket;
}
