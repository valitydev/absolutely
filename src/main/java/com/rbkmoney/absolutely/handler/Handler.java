package com.rbkmoney.absolutely.handler;

import com.rbkmoney.damsel.payment_processing.Invoice;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.swag.adapter.abs.model.Event;

public interface Handler {
    boolean accept(InvoiceChange change);
    Event handle(Invoice source, InvoiceChange change);
}
