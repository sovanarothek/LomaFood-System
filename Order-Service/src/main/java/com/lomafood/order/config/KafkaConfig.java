package com.lomafood.order.config;

import com.lomafood.order.dto.OrderEventDto;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name("order.created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderStatusTopic() {
        return TopicBuilder.name("order.status.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderCancelledTopic() {
        return TopicBuilder.name("order.cancelled")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public KafkaTemplate<String, OrderEventDto> kafkaTemplate(
            ProducerFactory<String, OrderEventDto> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
