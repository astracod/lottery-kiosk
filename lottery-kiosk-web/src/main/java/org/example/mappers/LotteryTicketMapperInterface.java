package org.example.mappers;

import org.example.dto.LotteryTicketDTO;
import org.example.entities.LotteryTicket;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LotteryTicketMapperInterface {
    LotteryTicketDTO toDto(LotteryTicket lotteryTicket);
    LotteryTicket toEntity(LotteryTicketDTO lotteryTicketDTO);
}
