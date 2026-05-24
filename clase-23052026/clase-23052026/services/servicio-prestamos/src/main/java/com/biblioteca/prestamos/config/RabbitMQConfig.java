package com.biblioteca.prestamos.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String COLA_EVENTOS = "prestamos.eventos";

    @Bean
    public Queue colaEventosPrestamos() {
        return new Queue(COLA_EVENTOS, true); // true = durable (sobrevive reinicios)
    }
}