package org.example.controller;

import org.example.dto.LotteryTicketDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface LotteryTicketControllerInterface {

    // Изменения для методов, которые используют RabbitMQ для получения данных
    ResponseEntity<List<LotteryTicketDTO>> getAllTickets() throws InterruptedException; // Этот метод будет просто инициировать запрос через RabbitMQ

    ResponseEntity<LotteryTicketDTO> getTicketById(Long id) throws InterruptedException; // Получение билета через RabbitMQ

    ResponseEntity<LotteryTicketDTO> createTicket(LotteryTicketDTO ticketDTO); // Создание билета через RabbitMQ

    ResponseEntity<LotteryTicketDTO> updateTicket(Long id, LotteryTicketDTO updatedTicket); // Обновление билета через RabbitMQ

    ResponseEntity<Void> deleteTicket(Long id); // Удаление билета через RabbitMQ
}
