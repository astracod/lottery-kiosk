package org.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TicketMessageForAllServiceDTO {
    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("requestType")
    private String requestType;
}
