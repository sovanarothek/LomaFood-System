package com.lomafood.payment.config;

import com.lomafood.payment.dto.PaymentEventDto;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic paymentSuccessTopic() {
        return TopicBuilder.name("payment.success")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name("payment.failed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentRefundedTopic() {
        return TopicBuilder.name("payment.refunded")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public KafkaTemplate<String, PaymentEventDto> kafkaTemplate(
            ProducerFactory<String, PaymentEventDto> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
