package com.rbkmoney.absolutely.service;

import com.rbkmoney.absolutely.AbsolutelyApplication;
import com.rbkmoney.absolutely.converter.TestData;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.domain.InvoicePaymentPending;
import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.sink.common.serialization.impl.PaymentEventPayloadSerializer;
import com.rbkmoney.swag.adapter.abs.api.AbsApi;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = AbsolutelyApplication.class)
public class InvoicingServiceTest {

    @MockBean
    private AbsApi absApi;

    @MockBean
    private HgService hgService;

    @Autowired
    private InvoicingService invoicingService;

    @Before
    public void setUp() {
        Mockito.when(hgService.get(anyString(), anyLong())).thenReturn(TestData.buildInvoiceRefund("1", "1"));
    }

    @Test
    public void handleEvents() {
        PaymentEventPayloadSerializer serializer = new PaymentEventPayloadSerializer();

        invoicingService.handleEvents(List.of(new MachineEvent()
                .setSourceId("inv1")
                .setData(Value.bin(serializer.serialize(
                        EventPayload.invoice_changes(List.of(
                                InvoiceChange.invoice_payment_change(
                                        new InvoicePaymentChange()
                                                .setId("1")
                                                .setPayload(InvoicePaymentChangePayload.invoice_payment_status_changed(
                                                        new InvoicePaymentStatusChanged()
                                                                .setStatus(InvoicePaymentStatus.pending(
                                                                        new InvoicePaymentPending()))))),
                                InvoiceChange.invoice_payment_change(
                                        new InvoicePaymentChange()
                                                .setId("1")
                                                .setPayload(InvoicePaymentChangePayload.invoice_payment_status_changed(
                                                        new InvoicePaymentStatusChanged()
                                                                .setStatus(InvoicePaymentStatus.captured(
                                                                        new InvoicePaymentCaptured()))))),
                                InvoiceChange.invoice_payment_change(
                                        new InvoicePaymentChange()
                                                .setId("1")
                                                .setPayload(InvoicePaymentChangePayload.invoice_payment_status_changed(
                                                        new InvoicePaymentStatusChanged()
                                                                .setStatus(InvoicePaymentStatus.failed(
                                                                        new InvoicePaymentFailed()
                                                                                .setFailure(OperationFailure.failure(
                                                                                        new Failure("code")))))))),
                                InvoiceChange.invoice_payment_change(
                                        new InvoicePaymentChange()
                                                .setId("1")
                                                .setPayload(InvoicePaymentChangePayload.invoice_payment_refund_change(
                                                        new InvoicePaymentRefundChange()
                                                                .setId("1")
                                                                .setPayload(InvoicePaymentRefundChangePayload.invoice_payment_refund_status_changed(
                                                                        new InvoicePaymentRefundStatusChanged()
                                                                                .setStatus(InvoicePaymentRefundStatus.succeeded(
                                                                                        new InvoicePaymentRefundSucceeded()))
                                                                ))
                                                )))

                        )))))));

        Mockito.verify(hgService, Mockito.times(3)).get(anyString(), anyLong());
        Mockito.verify(absApi, Mockito.times(1)).events(any(List.class));

    }
}