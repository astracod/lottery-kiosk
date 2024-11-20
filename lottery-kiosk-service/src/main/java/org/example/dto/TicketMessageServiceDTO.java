package org.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entities.LotteryTicket;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)  // Игнорировать неизвестные поля
public class TicketMessageServiceDTO {
    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("requestType")
    private String requestType;

    @JsonProperty("ticket")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LotteryTicket ticket;

    @JsonProperty("ticketList")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<LotteryTicket> ticketList;

    @JsonProperty("messageContent")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String messageContent;

}

