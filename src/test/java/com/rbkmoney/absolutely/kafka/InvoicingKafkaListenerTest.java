package com.rbkmoney.absolutely.kafka;

import com.rbkmoney.absolutely.service.InvoicingService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.anyList;

@Slf4j
public class InvoicingKafkaListenerTest extends AbstractKafkaTest {

    @Value("${kafka.topics.invoice.id}")
    public String topic;

    @MockBean
    private InvoicingService invoicingService;

    @Test
    public void listenEmptyChanges() throws InterruptedException {
        sendMessage(topic);
        Mockito.verify(invoicingService, Mockito.times(1)).handleEvents(anyList());
    }

}
