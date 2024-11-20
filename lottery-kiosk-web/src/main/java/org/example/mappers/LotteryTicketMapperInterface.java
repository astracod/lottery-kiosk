package org.example.mappers;

import org.example.dto.LotteryTicketDTO;
import org.example.dto.TicketMessageWebDTO;
import org.example.entities.LotteryTicket;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LotteryTicketMapperInterface {
    LotteryTicketDTO toDto(TicketMessageWebDTO lotteryTicket);

    TicketMessageWebDTO toServiceDto(LotteryTicketDTO lotteryTicketDTO);

    LotteryTicket toEntity(LotteryTicketDTO lotteryTicketDTO);

    List<LotteryTicketDTO> tiListDto(List<TicketMessageWebDTO> list);


}
