package org.example.repository;

import org.example.entities.LotteryTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LotteryTicketRepository extends JpaRepository<LotteryTicket, Long> {

    Optional<LotteryTicket> findById(Long ticketNumber);
}
