package com.rbkmoney.absolutely.handler;

import com.rbkmoney.absolutely.converter.PaymentConverter;
import com.rbkmoney.damsel.payment_processing.Invoice;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.swag.adapter.abs.ApiClient;
import com.rbkmoney.swag.adapter.abs.api.AbsApi;
import com.rbkmoney.swag.adapter.abs.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentHandler implements Handler<InvoiceChange, Invoice>  {

    private final PaymentConverter paymentConverter;
    private final AbsApi absApi;

    @Override
    public boolean accept(InvoiceChange change) {
        return change.isSetInvoicePaymentChange() &&
                change.getInvoicePaymentChange().getPayload().isSetInvoicePaymentStatusChanged() &&
                (change.getInvoicePaymentChange().getPayload().getInvoicePaymentStatusChanged().getStatus().isSetCaptured() ||
                        change.getInvoicePaymentChange().getPayload().getInvoicePaymentStatusChanged().getStatus().isSetFailed())
                ;
    }

    @Override
    public void handle(Invoice source, InvoiceChange change) {
        Payment payment = paymentConverter.convert(source, change.getInvoicePaymentChange().getId());
        absApi.payments(List);
    }

}
