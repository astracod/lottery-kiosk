package org.example.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@EnableRabbit
public class RabbitWebConfig {

    @Bean(name = "webConnectionFactory")
    public ConnectionFactory connectionFactory() {
        return new CachingConnectionFactory("localhost");
    }

    @Bean(name = "webMapper")
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        // Создание модуля JavaTimeModule для поддержки LocalDateTime
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // Настройка сериализатора и десериализатора для LocalDateTime
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

        // Регистрация модуля и отключение записи дат как временных меток
        return builder
                .modules(javaTimeModule)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    @Bean(name = "webRabbitTemplate")
    public RabbitTemplate rabbitTemplate(@Qualifier("webConnectionFactory") ConnectionFactory webConnectionFactory) {
        return new RabbitTemplate(webConnectionFactory);
    }

    @Bean(name = "webMappingJackson2HttpMessageConverter")
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(@Qualifier("webMapper") ObjectMapper objectMapper) {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }

    @Bean
    public Queue responseQueue() {
        return new Queue("responseQueue");
    }
}

