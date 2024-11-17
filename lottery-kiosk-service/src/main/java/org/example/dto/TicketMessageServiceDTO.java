package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entities.LotteryTicket;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketMessageServiceDTO {
    private String requestType;
    private LotteryTicket ticket;
}
