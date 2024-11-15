package org.example.services.impl;

import org.example.entities.LotteryTicket;
import org.example.repository.LotteryTicketRepository;
import org.example.services.interfaces.LotteryTicketServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LotteryTicketServiceImpl implements LotteryTicketServiceInterface {

    private LotteryTicketRepository lotteryTicketRepository;

    @Autowired
    public LotteryTicketServiceImpl(LotteryTicketRepository lotteryTicketRepository) {
        this.lotteryTicketRepository = lotteryTicketRepository;
    }

    @Override
    public List<LotteryTicket> printAllTickets() {
        return lotteryTicketRepository.findAll();
    }


    @Override
    public Optional<LotteryTicket> getTicketById(Long id) {
        return lotteryTicketRepository.findById(id);
    }

    @Override
    public Optional<LotteryTicket> createTicket(LotteryTicket newTicket) {
        Optional<LotteryTicket> answer = Optional.of(lotteryTicketRepository.save(newTicket));
        return answer;
    }

    @Override
    public boolean deleteTicket(Long id) {
        lotteryTicketRepository.deleteById(id);
        return lotteryTicketRepository.existsById(id);
    }

    @Override
    public void updateTicket(LotteryTicket updatedTicket) {
        lotteryTicketRepository.save(updatedTicket);
    }


}
