package org.example.controller;

import org.example.dto.LotteryTicketDTO;
import org.example.entities.LotteryTicket;
import org.example.mappers.LotteryTicketMapperInterface;
import org.example.services.MessageProducer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class LotteryTicketController { // довести до логического конца настройку очереди и проверить через постман

    private LotteryTicketMapperInterface mapper;
    private final MessageProducer messageProducer;

    public LotteryTicketController(LotteryTicketMapperInterface mapper, MessageProducer messageProducer) {
        this.mapper = mapper;
        this.messageProducer = messageProducer;
    }

    @GetMapping
    public ResponseEntity<Void> getAllTickets() {
        // Отправляем сообщение в очередь для получения всех билетов
        messageProducer.sendMessage("GET:AllTickets");

        // Пока что не возвращаем данные из БД, это будет обработано через очередь
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LotteryTicketDTO> getTicketById(@PathVariable("id") Long id) {
        LotteryTicket ticket = new LotteryTicket();
        ticket.setId(id);
        messageProducer.sendMessage("GET", ticket);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping
    public ResponseEntity<LotteryTicketDTO> createTicket(@RequestBody LotteryTicketDTO ticketDTO) {
        LotteryTicket ticket = mapper.toEntity(ticketDTO);
        messageProducer.sendMessage("CREATE", ticket);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateTicket(@PathVariable("id") Long id, @RequestBody LotteryTicketDTO updatedTicket) {
        // Преобразуем обновленное DTO в сущность
        LotteryTicket ticket = mapper.toEntity(updatedTicket);
        ticket.setId(id); // Устанавливаем ID для обновления конкретного тикета

        // Отправляем сообщение в очередь для обновления билета
        messageProducer.sendMessage(ticket); // Отправляем сущность для обработки

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable("id") Long id) {
        // Отправляем сообщение в очередь для удаления билета
        messageProducer.sendMessage("DELETE:DeleteTicket:" + id); // Отправляем ID для удаления

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
