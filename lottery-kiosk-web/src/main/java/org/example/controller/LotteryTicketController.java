package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.LotteryTicketDTO;
import org.example.entities.LotteryTicket;
import org.example.mappers.LotteryTicketMapperInterface;
import org.example.services.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class LotteryTicketController implements LotteryTicketControllerInterface {

    private final LotteryTicketMapperInterface mapper;
    private final MessageProducer messageProducer;
    private final ObjectMapper objectMapper;

    @Autowired
    public LotteryTicketController(LotteryTicketMapperInterface mapper,
                                   MessageProducer messageProducer,
                                   ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.messageProducer = messageProducer;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<List<LotteryTicket>> getAllTickets() {
        // Генерация уникального идентификатора корреляции
        String correlationId = generateCorrelationId();
        LotteryTicket buffer = new LotteryTicket();
        buffer.setId(-100L);  // Используем специальное значение для запроса всех билетов

        // Отправляем запрос на получение всех билетов
        messageProducer.sendMessage("GET", buffer, correlationId);

        // Ожидаем ответ
        String responseMessage = messageProducer.waitForResponse(correlationId);
        // возвращается null. Проверить с нормальным гпт

        if (responseMessage != null) {
            // Десериализация JSON-строки в список объектов LotteryTicket
            List<LotteryTicket> tickets = deserializeTickets(responseMessage);

            // Возвращаем список билетов в формате JSON
            return ResponseEntity.ok(tickets);
        } else {
            // В случае отсутствия ответа возвращаем статус ожидания
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<LotteryTicketDTO> getTicketById(@PathVariable("id") Long id) {
        LotteryTicket ticket = new LotteryTicket();
        ticket.setId(id);

        String correlationId = generateCorrelationId();

        // Отправляем запрос на получение тикета по ID
        messageProducer.sendMessage("GET", ticket, correlationId);

        // Ожидаем ответ
        String responseMessage = messageProducer.waitForResponse(correlationId);

        if (responseMessage != null) {
            // Преобразуем сообщение в нужный формат (например, в DTO)
            // И возвращаем его
            return ResponseEntity.ok(new LotteryTicketDTO());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    public ResponseEntity<LotteryTicketDTO> createTicket(@RequestBody LotteryTicketDTO ticketDTO) {
        LotteryTicket ticket = mapper.toEntity(ticketDTO);
        String correlationId = generateCorrelationId();

        // Отправляем запрос на создание билета
        messageProducer.sendMessage("CREATE", ticket, correlationId);

        // Ожидаем ответ
        String responseMessage = messageProducer.waitForResponse(correlationId);

        if (responseMessage != null) {
            // Преобразуем сообщение в нужный формат и возвращаем его
            return ResponseEntity.status(HttpStatus.CREATED).body(new LotteryTicketDTO());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<LotteryTicketDTO> updateTicket(Long id, LotteryTicketDTO updatedTicket) {
        LotteryTicket ticket = mapper.toEntity(updatedTicket);
        ticket.setId(id); // Устанавливаем ID для обновления
        String correlationId = generateCorrelationId();

        // Отправляем запрос на обновление билета
        messageProducer.sendMessage("UPDATE", ticket, correlationId);

        // Ожидаем ответ
        String responseMessage = messageProducer.waitForResponse(correlationId);

        if (responseMessage != null) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new LotteryTicketDTO());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Void> deleteTicket(Long id) {
        String correlationId = generateCorrelationId();

        // Отправляем запрос на удаление билета
        messageProducer.sendMessage("DELETE:" + id, correlationId);

        // Ожидаем ответ
        String responseMessage = messageProducer.waitForResponse(correlationId);

        if (responseMessage != null) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Метод для генерации уникального идентификатора корреляции
    private String generateCorrelationId() {
        return String.valueOf(System.currentTimeMillis());
    }

    private List<LotteryTicket> deserializeTickets(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<LotteryTicket>>() {
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Collections.emptyList();  // Возвращаем пустой список, если десериализация не удалась
        }
    }

}
