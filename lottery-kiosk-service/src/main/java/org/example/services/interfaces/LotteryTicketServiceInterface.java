package org.example.services.interfaces;

import org.example.entities.LotteryTicket;

import java.util.List;
import java.util.Optional;

public interface LotteryTicketServiceInterface {

    List<LotteryTicket> printAllTickets();
    Optional<LotteryTicket> getTicketById(Long id);

    Optional<LotteryTicket> createTicket(LotteryTicket newTicket);

    boolean deleteTicket(Long id);

}
