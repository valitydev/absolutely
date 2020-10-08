package com.rbkmoney.absolutely.config;

import com.rbkmoney.kafka.common.exception.handler.SeekToCurrentWithSleepBatchErrorHandler;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.absolutely.config.properties.KafkaConsumerProperties;
import com.rbkmoney.absolutely.config.properties.KafkaSslProperties;
import com.rbkmoney.absolutely.serde.SinkEventDeserializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.BatchErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaSslProperties.class)
public class KafkaConfig {

    private final KafkaConsumerProperties kafkaConsumerProperties;

    @Value("${kafka.client-id}")
    private String clientId;
    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public Map<String, Object> consumerConfigs(KafkaSslProperties kafkaSslProperties) {
        return createConsumerConfig(kafkaSslProperties);
    }

    private Map<String, Object> createConsumerConfig(KafkaSslProperties kafkaSslProperties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SinkEventDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerProperties.getGroupId());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaConsumerProperties.isEnableAutoCommit());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConsumerProperties.getAutoOffsetReset());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaConsumerProperties.getMaxPollRecords());
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaConsumerProperties.getSessionTimeoutMs());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, kafkaConsumerProperties.getMaxPollIntervalMs());
        configureSsl(props, kafkaSslProperties);
        return props;
    }

    private void configureSsl(Map<String, Object> props, KafkaSslProperties kafkaSslProperties) {
        if (kafkaSslProperties.isEnabled()) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name());
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, new File(kafkaSslProperties.getTrustStoreLocation()).getAbsolutePath());
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, kafkaSslProperties.getTrustStorePassword());
            props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, kafkaSslProperties.getKeyStoreType());
            props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, kafkaSslProperties.getTrustStoreType());
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, new File(kafkaSslProperties.getKeyStoreLocation()).getAbsolutePath());
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, kafkaSslProperties.getKeyStorePassword());
            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, kafkaSslProperties.getKeyPassword());
        }
    }

    @Bean
    public ConsumerFactory<String, MachineEvent> consumerFactory(KafkaSslProperties kafkaSslProperties) {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(kafkaSslProperties));
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, MachineEvent>> kafkaListenerContainerFactory(
            ConsumerFactory<String, MachineEvent> consumerFactory) {
        return createConcurrentFactory(consumerFactory, kafkaConsumerProperties.getInvoicingConcurrency());
    }

    private KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, MachineEvent>> createConcurrentFactory(
            ConsumerFactory<String, MachineEvent> consumerFactory, int threadsNumber) {
        ConcurrentKafkaListenerContainerFactory<String, MachineEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        initFactory(consumerFactory, threadsNumber, factory);
        return factory;
    }

    private <T> void initFactory(ConsumerFactory<String, T> consumerFactory, int threadsNumber, ConcurrentKafkaListenerContainerFactory<String, T> factory) {
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);
        factory.getContainerProperties().setAckOnError(false);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setBatchErrorHandler(kafkaErrorHandler());
        factory.setConcurrency(threadsNumber);
    }

    public BatchErrorHandler kafkaErrorHandler() {
        return new SeekToCurrentWithSleepBatchErrorHandler();
    }

}
