package com.rbkmoney.absolutely.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kafka.consumer")
public class KafkaConsumerProperties {

    private String autoOffsetReset;
    private boolean enableAutoCommit;
    private String groupId;
    private int maxPollRecords;
    private int maxPollIntervalMs;
    private int sessionTimeoutMs;
    private int invoicingConcurrency;

}
