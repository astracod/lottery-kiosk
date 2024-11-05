package org.example.controller;

import org.example.dto.LotteryTicketDTO;
import org.example.entities.LotteryTicket;
import org.example.mappers.LotteryTicketMapperInterface;
import org.example.services.interfaces.LotteryTicketServiceInterface;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
public class LotteryTicketController implements LotteryTicketControllerInterface {

    private LotteryTicketServiceInterface serviceInterface;

    private LotteryTicketMapperInterface mapper;

    public LotteryTicketController(LotteryTicketServiceInterface serviceInterface, LotteryTicketMapperInterface mapper) {
        this.mapper = mapper;
        this.serviceInterface = serviceInterface;
    }

    @GetMapping
    public ResponseEntity<List<LotteryTicketDTO>> getAllTickets() {
        List<LotteryTicket> tickets = serviceInterface.printAllTickets();

        // Преобразование списка LotteryTicket в список LotteryTicketDTO
        List<LotteryTicketDTO> ticketDTOs = tickets.stream()
                .map(mapper::toDto) // Преобразование в DTO
                .collect(Collectors.toList());

        return ResponseEntity.ok(ticketDTOs); // Возвращаем ResponseEntity со статусом 200 OK и списком DTO
    }

    @GetMapping("/{id}") // сначала проверить получение по айди
    public ResponseEntity<LotteryTicketDTO> getTicketById(@PathVariable("id") Long id) {
        Optional<LotteryTicket> ticketOptional = serviceInterface.getTicketById(id);

        if (ticketOptional.isPresent()) {
            LotteryTicketDTO ticketDTO = mapper.toDto(ticketOptional.get());
            return ResponseEntity.ok(ticketDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<LotteryTicketDTO> createTicket(@RequestBody LotteryTicketDTO ticketDTO) {
        // Преобразование DTO в сущность
        LotteryTicket newTicket = mapper.toEntity(ticketDTO);

        // Сохранение билета и получение Optional результата
        Optional<LotteryTicket> savedTicketOptional = serviceInterface.createTicket(newTicket);

        // Проверяем, было ли значение успешно сохранено
        return savedTicketOptional
                .map(savedTicket -> {
                    // Преобразуем обратно в DTO
                    LotteryTicketDTO savedTicketDTO = mapper.toDto(savedTicket);
                    // Возвращаем успешный ответ
                    return ResponseEntity.status(HttpStatus.CREATED).body(savedTicketDTO);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<LotteryTicketDTO> updateTicket(@PathVariable("id") Long id,@RequestBody LotteryTicketDTO updatedTicket) {
        Optional<LotteryTicket> ticketOptional = serviceInterface.getTicketById(id);
        if (ticketOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        LotteryTicket existingTicket = ticketOptional.get();


        LotteryTicket mappedEntity = mapper.toEntity(updatedTicket);

        mappedEntity.setId(existingTicket.getId());

        Optional<LotteryTicket> updatedEntity = serviceInterface.createTicket(mappedEntity);

        LotteryTicketDTO responseDTO = mapper.toDto(updatedEntity.get());

        return ResponseEntity.ok(responseDTO);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable("id") Long id) {
        // Шаг 1: Проверяем, существует ли сущность с указанным ID
        Optional<LotteryTicket> ticketOptional = serviceInterface.getTicketById(id);
        if (ticketOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Возвращаем 404, если сущность не найдена
        }

        // Шаг 2: Удаляем сущность
        boolean result = serviceInterface.deleteTicket(id);

        // Шаг 3: Повторно проверяем, осталась ли сущность
        if (result) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Возвращаем 500, если сущность осталась
        }

        // Успешное удаление
        return ResponseEntity.noContent().build(); // Возвращаем 204
    }




}
