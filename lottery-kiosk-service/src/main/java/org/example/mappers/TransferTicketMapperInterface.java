package org.example.mappers;

import org.example.dto.TicketMessageServiceDTO;
import org.example.entities.LotteryTicket;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransferTicketMapperInterface {

    TicketMessageServiceDTO toDto(LotteryTicket ticket);
}
