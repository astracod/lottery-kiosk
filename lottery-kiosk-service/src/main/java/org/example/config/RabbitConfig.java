package org.example.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

@Configuration
@EnableRabbit
public class RabbitConfig {

    @Bean(name = "connectionFactory")
    public ConnectionFactory connectionFactory() {
        return new CachingConnectionFactory("localhost");
    }

    @Bean
    public RabbitAdmin rabbitAdmin(@Qualifier("connectionFactory") ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean(name = "serviceMapper")
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  // Этот модуль необходим для работы с LocalDateTime
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        return objectMapper;
    }

    @Bean(name = "rabbitTemplate")
    public RabbitTemplate rabbitTemplate(@Qualifier("connectionFactory") ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean(name = "serviceMappingJackson2HttpMessageConverter")
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(@Qualifier("serviceMapper") ObjectMapper objectMapper) {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }

    @Bean
    public Queue requestQueue() {
        return new Queue("requestQueue");
    }


    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange("exchangeName");
    }

    @Bean
    public Binding createBinding(@Qualifier("requestQueue") Queue requestQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(requestQueue).to(topicExchange).with("request.create");
    }

    @Bean
    public Binding updateBinding(@Qualifier("requestQueue") Queue requestQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(requestQueue).to(topicExchange).with("request.update");
    }

    @Bean
    public Binding deleteBinding(@Qualifier("requestQueue") Queue requestQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(requestQueue).to(topicExchange).with("request.delete");
    }

    @Bean
    public Binding getBinding(@Qualifier("requestQueue") Queue requestQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(requestQueue).to(topicExchange).with("request.get");
    }


}
