package org.example.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LotteryTicketDTO {
    private Long id;
    private String ticketNumber;
    private LocalDateTime purchaseDate;
    private BigDecimal price;
    private Boolean winningStatus;
    private BigDecimal prizeAmount;
}
