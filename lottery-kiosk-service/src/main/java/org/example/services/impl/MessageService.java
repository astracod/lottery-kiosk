package org.example.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.TicketMessageServiceDTO;
import org.example.entities.LotteryTicket;
import org.example.mappers.TransferTicketMapperInterface;
import org.example.services.interfaces.LotteryTicketServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    private final RabbitTemplate rabbitTemplate;
    private final LotteryTicketServiceInterface lotteryTicketService;

    private final TransferTicketMapperInterface transferMapper;

    private final ObjectMapper objectMapper;

    // Конструктор с инжекцией зависимостей
    public MessageService(@Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate,
                          LotteryTicketServiceInterface lotteryTicketService,
                          TransferTicketMapperInterface transferMapper,
                          @Qualifier("serviceMapper") ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.lotteryTicketService = lotteryTicketService;
        this.transferMapper = transferMapper;
        this.objectMapper = objectMapper;
    }

    // Метод для отправки сообщений в очередь RabbitMQ
    public void sendMessage(String queueName, String message) {
        rabbitTemplate.convertAndSend(queueName, message);
        System.out.println("Message sent to Queue : "+queueName+" Message : " + message);
    }

    // Метод для получения сообщений из очереди
    @RabbitListener(queues = "requestQueue")
    public void receiveMessage(String jsonTicket) throws InterruptedException {

        System.out.println("Десериализуем JSON: " + jsonTicket);
        // Десериализация сообщения в DTO
        TicketMessageServiceDTO ticketMessage = deserializeTicketData(jsonTicket);

        // Логирование входящего сообщения
        System.out.println("Received message to Service module : " + ticketMessage);


        String requestType = ticketMessage.getRequestType();

        try {
            // Проверка типа запроса для получения всех билетов
            if ("GET_ALL".equals(requestType)) {
                ticketMessage.setTicketList(handleGetAllTicket());
                String jsonListString = serializeToJson(ticketMessage);
                sendMessage("responseQueue", jsonListString);
            }else {
                LotteryTicket ticketData = ticketMessage.getTicket();
                // Обработка остальных типов запросов
                switch (requestType) {
                    case "CREATE" -> handleCreate(ticketData);
                    case "GET_BY_ID" -> {
                        ticketMessage.setTicket(handleGetByID(ticketData));
                        String jsonListString = serializeToJson(ticketMessage);
                        sendMessage("responseQueue", jsonListString);
                    }
                    case "UPDATE" -> handleUpdate(ticketData);
                    case "DELETE" -> handleDelete(ticketData);
                    default -> System.out.println("Unknown request type: " + requestType);
                }
            }

        } catch (Exception e) {
            System.out.printf("Обработан запрос типа: %s, сообщение: %s%n", requestType, e.getMessage());
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
    private LotteryTicket handleGetByID(LotteryTicket ticketData) {

        Optional<LotteryTicket> ticket = lotteryTicketService.getTicketById(ticketData.getId());
        if (ticket.isPresent()) {
            System.out.println("Ticket found: " + ticket.get());
            return ticket.get();
        } else {
            System.out.println("Ticket not found with ID: " + ticketData.getId());
            return new LotteryTicket();
        }
    }

    private List<LotteryTicket> handleGetAllTicket() {
        List<LotteryTicket> allTickets = lotteryTicketService.printAllTickets();
        System.out.println("All tickets have been retrieved.");
        return allTickets;
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
            logger.error("Ошибка при десериализации: {}", e.getMessage(), e);
            return null;
        }
    }

    private String serializeToJson(TicketMessageServiceDTO message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String serializeListToJson(List<TicketMessageServiceDTO> messages) {
        try {
            return objectMapper.writeValueAsString(messages);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
