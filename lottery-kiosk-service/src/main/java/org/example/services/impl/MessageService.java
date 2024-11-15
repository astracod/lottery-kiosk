package org.example.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.TicketMessageServiceDTO;
import org.example.entities.LotteryTicket;
import org.example.services.interfaces.LotteryTicketServiceInterface;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private final RabbitTemplate rabbitTemplate;
    private final LotteryTicketServiceInterface lotteryTicketService;

    private final ObjectMapper objectMapper;

    // Конструктор с инжекцией зависимостей
    public MessageService(RabbitTemplate rabbitTemplate, LotteryTicketServiceInterface lotteryTicketService, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.lotteryTicketService = lotteryTicketService;
        this.objectMapper = objectMapper;
    }

    // Метод для отправки сообщений в очередь RabbitMQ
    public void sendMessage(String queueName, String message) {
        rabbitTemplate.convertAndSend(queueName, message);
        System.out.println("Message sent: " + message);
    }

    // Метод для получения сообщений из очереди
    @RabbitListener(queues = "lotteryQueue")
    public void receiveMessage(String message) {
        // Логирование входящего сообщения
        System.out.println("Received message: " + message);

        // Десериализация сообщения в DTO
        TicketMessageServiceDTO messageServiceDTO = deserializeTicketData(message);
        if (messageServiceDTO == null) {
            System.out.println("Error deserializing message.");
            return;
        }

        String requestType = messageServiceDTO.getRequestType();
        LotteryTicket ticketData = messageServiceDTO.getTicket(); // ticketData теперь строка или объект

        try {
            // Обработка запроса в зависимости от типа
            switch (requestType) {
                case "CREATE" -> handleCreate(ticketData);  // Передаем полный объект для создания
                case "GET" -> {
                    List<LotteryTicket> tickets = handleGet(ticketData);
                    String response = ticketsToString(tickets);  // Конвертируем список билетов в JSON
                    sendMessage("responseQueue", response);  // Отправляем JSON-ответ в очередь
                }
                case "UPDATE" -> handleUpdate(ticketData);  // Передаем полный объект для обновления
                case "DELETE" -> handleDelete(ticketData);  // Передаем ID для удаления
                default -> System.out.println("Unknown request type: " + requestType);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + message);
            // Обработать ошибку (например, отправить в другую очередь)
        }
    }

    private String ticketsToString(List<LotteryTicket> tickets) {
        StringBuilder stringBuilder = new StringBuilder();

        tickets.forEach(ticket -> stringBuilder
                .append(ticket.toString())
                .append(System.lineSeparator()));  // добавляем новую строку между билетами для удобства чтения

        return stringBuilder.toString();
    }

    // Обработка запроса на создание нового билета
    private void handleCreate(LotteryTicket newTicket) {
        if (newTicket != null) {
            lotteryTicketService.createTicket(newTicket);
            System.out.println("Ticket created: " + newTicket);
        } else {
            System.out.println("Failed to deserialize ticket data for creation.");
        }
    }

    // Обработка запроса на получение билета
    private List<LotteryTicket> handleGet(LotteryTicket ticketData) {
        if (ticketData.getId() == -100) {
            // Запрос на получение всех билетов
            List<LotteryTicket> allTickets = lotteryTicketService.printAllTickets(); // Предполагается, что метод возвращает список
            System.out.println("All tickets have been retrieved.");
            return allTickets;
        } else {
            // Запрос на получение конкретного билета по ID
            Optional<LotteryTicket> ticket = lotteryTicketService.getTicketById(ticketData.getId());
            if (ticket.isPresent()) {
                System.out.println("Ticket found: " + ticket.get());
                return Collections.singletonList(ticket.get()); // Возвращаем билет как список с одним элементом
            } else {
                System.out.println("Ticket not found with ID: " + ticketData.getId());
                return Collections.emptyList(); // Пустой список, если билет не найден
            }
        }
    }


    // Обработка запроса на обновление данных билета
    private void handleUpdate(LotteryTicket updatedTicket) {
        if (updatedTicket != null) {
            lotteryTicketService.updateTicket(updatedTicket);
            System.out.println("Ticket updated: " + updatedTicket);
        } else {
            System.out.println("Failed to deserialize ticket data for update.");
        }
    }

    // Обработка запроса на удаление билета
    private void handleDelete(LotteryTicket deleteTicket) {
        try {
            boolean deleted = lotteryTicketService.deleteTicket(deleteTicket.getId());
            if (deleted) {
                System.out.println("Ticket deleted with ID: " + deleteTicket.getId());
            } else {
                System.out.println("Ticket not found for deletion with ID: " + deleteTicket.getId());
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ticket ID format for deletion: " + deleteTicket.getId());
        }
    }

    // Метод для десериализации JSON в объект LotteryTicket
    private TicketMessageServiceDTO deserializeTicketData(String ticketData) {

        try {
            return objectMapper.readValue(ticketData, TicketMessageServiceDTO.class);
        } catch (JsonProcessingException e) {
            System.out.println("Failed to deserialize ticket data: " + ticketData);
            System.out.println("Error from Service: "+ e);
            return null;
        }
    }
}
