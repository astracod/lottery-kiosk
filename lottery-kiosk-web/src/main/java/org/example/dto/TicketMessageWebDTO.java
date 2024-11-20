package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketMessageWebDTO {
    private String messageId; // Уникальный идентификатор сообщения
    private String requestType; // Тип запроса (CREATE, UPDATE, DELETE, RESPONSE)
    private LotteryTicketDTO ticket;
    private List<LotteryTicketDTO> ticketList;  // Список билетов
    private String messageContent;
}
