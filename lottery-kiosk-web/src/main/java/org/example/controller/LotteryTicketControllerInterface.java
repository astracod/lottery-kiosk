package org.example.controller;

import org.example.dto.LotteryTicketDTO;
import org.example.entities.LotteryTicket;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface LotteryTicketControllerInterface {

    ResponseEntity<List<LotteryTicketDTO>> getAllTickets();

    ResponseEntity<LotteryTicketDTO> getTicketById(Long id);

    ResponseEntity<LotteryTicketDTO> createTicket(LotteryTicketDTO ticketDTO);

    ResponseEntity<LotteryTicketDTO> updateTicket(@PathVariable Long id, @RequestBody LotteryTicketDTO updatedTicket);

    ResponseEntity<Void> deleteTicket(@PathVariable Long id);

}
