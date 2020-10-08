package com.rbkmoney.absolutely.config;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.sink.common.serialization.BinaryDeserializer;
import com.rbkmoney.sink.common.serialization.impl.PaymentEventPayloadDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SerializationConfig {

    @Bean
    public BinaryDeserializer<EventPayload> paymentEventPayloadDeserializer() {
        return new PaymentEventPayloadDeserializer();
    }
}
