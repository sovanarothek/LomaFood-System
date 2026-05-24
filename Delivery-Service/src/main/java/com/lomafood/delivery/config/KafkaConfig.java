package com.lomafood.delivery.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic deliveryEventsTopic() {
        return new NewTopic("delivery-events", 1, (short) 1);
    }

    @Bean
    public NewTopic notificationEventsTopic() {
        return new NewTopic("notification-events", 1, (short) 1);
    }
}
