package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.LotteryTicketDTO;
import org.example.dto.TicketMessageWebDTO;
import org.example.entities.LotteryTicket;
import org.example.mappers.LotteryTicketMapperInterface;
import org.example.services.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/tickets")
public class LotteryTicketController implements LotteryTicketControllerInterface {

    private final LotteryTicketMapperInterface mapper;
    private final QueueService queueService;
    private final ObjectMapper objectMapper;

    @Autowired
    public LotteryTicketController(LotteryTicketMapperInterface mapper,
                                   QueueService queueService,
                                   @Qualifier("webMapper") ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.queueService = queueService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<List<LotteryTicketDTO>> getAllTickets() {
        List<LotteryTicketDTO> response = queueService.getAllTickets();
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<LotteryTicketDTO> getTicketById(@PathVariable("id") Long id) {
        TicketMessageWebDTO response = queueService.getTicketById(id);
        return ResponseEntity.ok(response.getTicket());
    }

    @PostMapping
    public ResponseEntity<LotteryTicketDTO> createTicket(@RequestBody LotteryTicketDTO ticketDTO) {
        TicketMessageWebDTO ticket = mapper.toServiceDto(ticketDTO);
        ticket.setMessageId(generateCorrelationId());
        ticket.setRequestType("CREATE");

        // Отправляем запрос на создание билета
        queueService.sendToRequestQueue(ticket);

        // Разобраться с обратной отправкой и получением ответа

        // Ожидаем ответ
        TicketMessageWebDTO responseMessage = queueService.receiveFromResponseQueue();

        if (responseMessage != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(responseMessage));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<LotteryTicketDTO> updateTicket(Long id, LotteryTicketDTO updatedTicket) {
        updatedTicket.setId(id);
        TicketMessageWebDTO ticket = mapper.toServiceDto(updatedTicket);
        ticket.setMessageId(generateCorrelationId());
        ticket.setRequestType("UPDATE");

        // Отправляем запрос на обновление билета
        queueService.sendToRequestQueue(ticket);

        // Ожидаем ответ
        TicketMessageWebDTO responseMessage = queueService.receiveFromResponseQueue();

        if (responseMessage != null) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(mapper.toDto(responseMessage));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Void> deleteTicket(Long id) {
        LotteryTicketDTO lotteryTicketDTO = new LotteryTicketDTO();
        lotteryTicketDTO.setId(id);
        TicketMessageWebDTO ticketMessageDTO = new TicketMessageWebDTO();
        ticketMessageDTO.setMessageId(generateCorrelationId());
        ticketMessageDTO.setRequestType("DELETE");
        ticketMessageDTO.setTicket(lotteryTicketDTO);
        // Отправляем запрос на удаление билета
        queueService.sendToRequestQueue(ticketMessageDTO);

        // Ожидаем ответ
        TicketMessageWebDTO responseMessage = queueService.receiveFromResponseQueue();

        if (responseMessage != null) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Метод для генерации уникального идентификатора корреляции
    private String generateCorrelationId() {
        return String.valueOf(UUID.randomUUID());
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
