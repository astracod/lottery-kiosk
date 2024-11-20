package org.example.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LotteryTicketDTO implements Serializable {
    private Long id;
    private String ticketNumber;
    private LocalDateTime purchaseDate;
    private BigDecimal price;
    private Boolean winningStatus;
    private BigDecimal prizeAmount;
}
