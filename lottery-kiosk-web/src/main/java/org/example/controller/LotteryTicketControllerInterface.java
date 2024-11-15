package org.example.controller;

import org.example.dto.LotteryTicketDTO;
import org.example.entities.LotteryTicket;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface LotteryTicketControllerInterface {

    // Изменения для методов, которые используют RabbitMQ для получения данных
    ResponseEntity<List<LotteryTicket>> getAllTickets(); // Этот метод будет просто инициировать запрос через RabbitMQ

    ResponseEntity<LotteryTicketDTO> getTicketById(Long id); // Получение билета через RabbitMQ

    ResponseEntity<LotteryTicketDTO> createTicket(LotteryTicketDTO ticketDTO); // Создание билета через RabbitMQ

    ResponseEntity<LotteryTicketDTO> updateTicket(Long id, LotteryTicketDTO updatedTicket); // Обновление билета через RabbitMQ

    ResponseEntity<Void> deleteTicket(Long id); // Удаление билета через RabbitMQ
}
