package org.example.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.LotteryTicketDTO;
import org.example.dto.TicketMessageServiceDTO;
import org.example.dto.TicketMessageWebDTO;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
public class QueueService {

    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;

    // Хранилище для корреляции запросов и ответов
    private final ConcurrentHashMap<String, TicketMessageWebDTO> responseStorage = new ConcurrentHashMap<>();


    @Autowired
    public QueueService(@Qualifier("webRabbitTemplate") RabbitTemplate rabbitTemplate,
                        @Qualifier("webMapper") ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendToRequestQueue(TicketMessageWebDTO ticketMessage) {
        String correlationId = ticketMessage.getMessageId();
        // Сохраняем ссылку на запрос в responseStorage, чтобы найти ответ позже
        responseStorage.put(correlationId, null);  // Изначально ответа нет
        rabbitTemplate.convertAndSend("requestQueue", ticketMessage);
    }


    public TicketMessageWebDTO receiveFromResponseQueue() {
        // Десериализуем и получаем ответ из очереди ответов
        String jsonResponse = String.valueOf(rabbitTemplate.receiveAndConvert("responseQueue"));
        System.out.println("Получаемая строка ответа контроллера " + jsonResponse);
        return deserializeString(jsonResponse);
    }

    public List<LotteryTicketDTO> getAllTickets() {

        TicketMessageWebDTO ticketMessageDTO = new TicketMessageWebDTO();
        ticketMessageDTO.setMessageId(UUID.randomUUID().toString());
        ticketMessageDTO.setRequestType("GET_ALL");
        responseStorage.put(ticketMessageDTO.getMessageId(), ticketMessageDTO);
        String jsonTicket = serializeToJson(ticketMessageDTO);
        // Отправляем запрос на получение всех билетов
        rabbitTemplate.convertAndSend("requestQueue", jsonTicket);
        TicketMessageWebDTO dto = receiveFromResponseQueue();
        return dto.getTicketList();
    }

    public TicketMessageWebDTO getTicketById(Long ticketId) {
        // Логика для запроса конкретного билета по ID
        TicketMessageWebDTO requestMessage = new TicketMessageWebDTO();
        requestMessage.setMessageId(UUID.randomUUID().toString());
        requestMessage.setRequestType("GET_BY_ID");
        LotteryTicketDTO lotteryTicket = new LotteryTicketDTO();
        lotteryTicket.setId(ticketId);
        requestMessage.setTicket(lotteryTicket);

        responseStorage.put(requestMessage.getMessageId(), requestMessage);
        String jsonTicket = serializeToJson(requestMessage);
        // Отправляем запрос в очередь и ожидаем ответ
        rabbitTemplate.convertAndSend("requestQueue", jsonTicket);
        // Получаем ответ из очереди


        // метод для получения билета
        String jsonResponse = String.valueOf(rabbitTemplate.receiveAndConvert("responseQueue"));
        TicketMessageWebDTO dto = deserializeString(jsonResponse);
        return dto;
    }

    private String serializeToJson(TicketMessageWebDTO message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private TicketMessageWebDTO deserializeString(String ticketData) {

        try {
            return objectMapper.readValue(ticketData, TicketMessageWebDTO.class);
        } catch (JsonProcessingException e) {
            System.out.println("Failed to deserialize ticket data: " + ticketData);
            System.out.println("Error from Controller Service: " + e);
            return null;
        }
    }


}
