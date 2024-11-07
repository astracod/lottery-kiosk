package org.example.services.impl;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MessageConsumer {

    @RabbitListener(queues = "ticketQueue") // Убедитесь, что название очереди совпадает
    public void receiveMessage(String message) {
        // Разделяем сообщение на префикс и тело
        String[] parts = message.split(":", 2);
        if (parts.length == 2) {
            String requestType = parts[0];
            String ticketData = parts[1]; // Здесь нужно будет десериализовать ticketData в LotteryTicket

            // В зависимости от requestType направляем ticketData в соответствующий метод обработки
            switch (requestType) {
                case "CREATE" -> handleCreate(ticketData);
                case "GET" -> handleGet(ticketData);
                case "UPDATE" -> handleUpdate(ticketData);
                case "DELETE" -> handleDelete(ticketData);
                default ->
                    // Логика для неизвестного типа запроса
                        System.out.println("Unknown request type: " + requestType);
            }
        } else {
            System.out.println("Invalid message format: " + message);
        }
    }

    private void handleCreate(String ticketData) {
        // Преобразуйте ticketData в объект LotteryTicket и выполните операцию создания
    }

    private void handleGet(String ticketData) {
        // Преобразуйте ticketData и выполните операцию получения
    }

    private void handleUpdate(String ticketData) {
        // Преобразуйте ticketData и выполните операцию обновления
    }

    private void handleDelete(String ticketData) {
        // Преобразуйте ticketData и выполните операцию удаления
    }
}





