package com.rbkmoney.absolutely.handler;

import com.rbkmoney.absolutely.converter.AdjustmentConverter;
import com.rbkmoney.damsel.payment_processing.Invoice;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.swag.adapter.abs.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdjustmentHandler implements Handler  {

    private final AdjustmentConverter adjustmentConverter;

    @Override
    public boolean accept(InvoiceChange change) {
        return change.isSetInvoicePaymentChange() &&
                change.getInvoicePaymentChange().getPayload().isSetInvoicePaymentAdjustmentChange() &&
                change.getInvoicePaymentChange().getPayload().getInvoicePaymentAdjustmentChange().getPayload().isSetInvoicePaymentAdjustmentStatusChanged() &&
                        change.getInvoicePaymentChange().getPayload().getInvoicePaymentAdjustmentChange().getPayload().getInvoicePaymentAdjustmentStatusChanged().getStatus().isSetCaptured();

    }

    @Override
    public Event handle(Invoice source, InvoicePaymentChange paymentChange) {
        return adjustmentConverter.convert(source, paymentChange.getId(),paymentChange.getPayload().getInvoicePaymentAdjustmentChange().getId());
    }
}
