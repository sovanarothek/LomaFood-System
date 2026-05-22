package com.lomafood.rider.config;

import com.lomafood.rider.dto.RiderEventDto;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic riderLocationTopic() {
        return TopicBuilder.name("rider.location.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic riderAssignedTopic() {
        return TopicBuilder.name("rider.assigned")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public KafkaTemplate<String, RiderEventDto> kafkaTemplate(
            ProducerFactory<String, RiderEventDto> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
