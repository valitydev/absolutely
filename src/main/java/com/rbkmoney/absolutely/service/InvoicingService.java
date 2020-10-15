package com.rbkmoney.absolutely.service;

import com.rbkmoney.absolutely.handler.Handler;
import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import com.rbkmoney.swag.adapter.abs.api.AbsApi;
import com.rbkmoney.swag.adapter.abs.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoicingService {

    private final AbsApi absApi;
    private final HgService hgService;
    private final MachineEventParser<EventPayload> parser;
    private final List<Handler> handlers;

    public void handleEvents(List<MachineEvent> machineEvents) {
        List<Event> events = new ArrayList<>();
        machineEvents.forEach(e -> {
            EventPayload eventPayload = parser.parse(e);
            eventPayload.getInvoiceChanges().forEach(invoiceChange ->
                    handlers.stream().filter(h -> h.accept(invoiceChange)).findFirst().ifPresent(handler -> {
                            Invoice invoice = hgService.get(e.getSourceId(), e.getEventId());
                            Event event = handler.handle(invoice, invoiceChange);
                            events.add(event);
                    }));
        });
        absApi.events(events);
    }

}
