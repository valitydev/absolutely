package com.rbkmoney.absolutely.service;

import com.rbkmoney.damsel.payment_processing.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HgService {
    private final InvoicingSrv.Iface invoicingClient;

    @SneakyThrows
    public Invoice get(String invoiceId, long sequenceId) {
        return invoicingClient.get(buildUserInfo(), invoiceId, buildEventRange(sequenceId));
    }

    private EventRange buildEventRange(long eventId) {
        return new EventRange().setLimit((int) eventId);
    }

    private UserInfo buildUserInfo() {
        return new UserInfo("abs", UserType.service_user(new ServiceUser()));
    }

}
