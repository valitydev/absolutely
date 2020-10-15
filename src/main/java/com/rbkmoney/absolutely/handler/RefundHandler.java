package com.rbkmoney.absolutely.handler;

import com.rbkmoney.absolutely.converter.RefundConverter;
import com.rbkmoney.damsel.payment_processing.Invoice;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.swag.adapter.abs.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefundHandler implements Handler  {

    private final RefundConverter refundConverter;

    @Override
    public boolean accept(InvoiceChange change) {
        return change.isSetInvoicePaymentChange() &&
                change.getInvoicePaymentChange().getPayload().isSetInvoicePaymentRefundChange() &&
                change.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getPayload().isSetInvoicePaymentRefundStatusChanged() &&
                        change.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getPayload().getInvoicePaymentRefundStatusChanged().getStatus().isSetSucceeded();

    }

    @Override
    public Event handle(Invoice source, InvoiceChange change) {
        return refundConverter.convert(source, change.getInvoicePaymentChange().getId(), change.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getId());
    }
}
