package org.example.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.TicketMessageDTO;
import org.example.entities.LotteryTicket;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;
    private final Map<String, String> correlationResponses = new ConcurrentHashMap<>();  // Для хранения ответов по идентификаторам

    private final ObjectMapper objectMapper;

    public MessageProducer(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(LotteryTicket ticket, String correlationId) {
        rabbitTemplate.convertAndSend("lotteryQueue", ticket, message -> {
            message.getMessageProperties().setCorrelationId(correlationId);  // Устанавливаем идентификатор корреляции
            return message;
        });
    }

    public void sendMessage(String message, String correlationId) {
        rabbitTemplate.convertAndSend("lotteryQueue", message, m -> {
            m.getMessageProperties().setCorrelationId(correlationId);  // Устанавливаем идентификатор корреляции
            return m;
        });
    }

    public void sendMessage(String requestType, LotteryTicket ticket, String correlationId) {
        TicketMessageDTO message = new TicketMessageDTO(requestType, ticket);

        // Сериализуем объект в JSON
        String jsonMessage = serializeToJson(message);
        rabbitTemplate.convertAndSend("lotteryQueue", jsonMessage, m -> {
            m.getMessageProperties().setCorrelationId(correlationId);  // Устанавливаем идентификатор корреляции
            return m;
        });
    }

    private String serializeToJson(TicketMessageDTO message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sendMessage(String queueName, Object message, String correlationId) {
        try {
            rabbitTemplate.convertAndSend(queueName, message, m -> {
                m.getMessageProperties().setCorrelationId(correlationId);
                return m;
            });
        } catch (AmqpException e) {
            // Обработать исключение отправки
            System.err.println("Failed to send message to queue " + queueName + ": " + e.getMessage());
        }
    }


    // Метод для получения ответа с определенным идентификатором корреляции
    public String waitForResponse(String correlationId) {
        try {
            long startTime = System.currentTimeMillis();
            while (!correlationResponses.containsKey(correlationId)) {
                if (System.currentTimeMillis() - startTime > 10000) {  // Timeout в 10 секунд
                    break;
                }
                Thread.sleep(100);  // Пауза для предотвращения высокой загрузки процессора
            }
            return correlationResponses.remove(correlationId);  // Возвращаем ответ и удаляем его
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    // Метод для обработки сообщений, полученных через RabbitListener
    public void handleResponse(String correlationId, String responseMessage) {
        correlationResponses.put(correlationId, responseMessage);  // Сохраняем ответ по идентификатору корреляции
    }
}
