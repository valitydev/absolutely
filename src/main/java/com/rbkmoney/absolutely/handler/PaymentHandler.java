package com.rbkmoney.absolutely.handler;

import com.rbkmoney.absolutely.converter.PaymentConverter;
import com.rbkmoney.damsel.payment_processing.Invoice;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.swag.adapter.abs.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentHandler implements Handler  {

    private final PaymentConverter paymentConverter;

    @Override
    public boolean accept(InvoiceChange change) {
        return change.isSetInvoicePaymentChange() &&
                change.getInvoicePaymentChange().getPayload().isSetInvoicePaymentStatusChanged() &&
                (change.getInvoicePaymentChange().getPayload().getInvoicePaymentStatusChanged().getStatus().isSetCaptured() ||
                        change.getInvoicePaymentChange().getPayload().getInvoicePaymentStatusChanged().getStatus().isSetFailed())
                ;
    }

    @Override
    public Event handle(Invoice source, InvoicePaymentChange paymentChange) {
        return paymentConverter.convert(source, paymentChange.getId());
    }
}
