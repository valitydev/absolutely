package com.rbkmoney.absolutely.service;

import com.rbkmoney.absolutely.handler.Handler;
import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoicingService {

    private final MachineEventParser<EventPayload> parser;
    private final InvoicingSrv.Iface invoicingClient;
    private final List<Handler> handlers;

    public void handleEvents(List<MachineEvent> events) {
        events.forEach(e -> {
            EventPayload eventPayload = parser.parse(e);
            for (int i = 0; i < eventPayload.getInvoiceChanges().size(); ++i) {
                InvoiceChange invoiceChange = eventPayload.getInvoiceChanges().get(i);
                handlers.stream().filter(h -> h.accept(invoiceChange)).findFirst().ifPresent(handler -> {
                    try {
                        Invoice invoice = invoicingClient.get(buildUserInfo(), e.getSourceId(), buildEventRange(e.getEventId()));
                        handler.handle(invoice, invoiceChange);
                    } catch (TException e1) {
                        throw new RuntimeException(e1);
                    }
                });
            }
        });

    }

    private EventRange buildEventRange(long eventId) {
        return new EventRange().setLimit((int) eventId);
    }

    private UserInfo buildUserInfo() {
        return new UserInfo("abs", UserType.service_user(new ServiceUser()));
    }


}
