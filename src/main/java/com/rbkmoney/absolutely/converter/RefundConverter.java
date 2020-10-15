package com.rbkmoney.absolutely.converter;

import com.rbkmoney.absolutely.utils.CashFlowUtils;
import com.rbkmoney.absolutely.utils.ErrorUtils;
import com.rbkmoney.absolutely.utils.InvoiceUtils;
import com.rbkmoney.absolutely.utils.TimeUtils;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefund;
import com.rbkmoney.damsel.payment_processing.InvoiceRefundSession;
import com.rbkmoney.swag.adapter.abs.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RefundConverter {

    private final InvoiceDetailsConverter invoiceDetailsConverter;
    private final TransactionInfoConverter transactionInfoConverter;

    public Refund convert(com.rbkmoney.damsel.payment_processing.Invoice invoice, String paymentId, String refundId) {
        InvoicePayment invoicePayment = InvoiceUtils.extractPayment(invoice, paymentId);
        InvoicePaymentRefund invoicePaymentRefund = InvoiceUtils.extractRefund(invoicePayment, refundId);
        var payment = invoicePayment.getPayment();
        var refund = invoicePaymentRefund.getRefund();
        TransactionInfo transactionInfo = getTransactionInfo(invoicePaymentRefund.getSessions());
        InvoicePaymentRefundStatus status = refund.getStatus();
        var fees = invoicePaymentRefund.getCashFlow() != null ? CashFlowUtils.getFees(invoicePaymentRefund.getCashFlow()) : null;

        return new Refund()
                .id(refundId)
                .paymentID(paymentId)
                .invoiceID(invoice.getInvoice().getId())
                .createdAt(TimeUtils.toOffsetDateTime(refund.getCreatedAt()))
                .domainRevision(refund.getDomainRevision())
                .partyRevision(refund.getPartyRevision())
                .partyID(payment.getOwnerId())
                .shopID(payment.getShopId())
                .status(Refund.StatusEnum.fromValue(status.getSetField().getFieldName()))
                .amount(refund.getCash().getAmount())
                .currency(refund.getCash().getCurrency().getSymbolicCode())
                .fee(CashFlowUtils.getFee(fees))
                .providerFee(CashFlowUtils.getProviderFee(fees))
                .externalFee(CashFlowUtils.getExternalFee(fees))
                .invoiceDetails(invoiceDetailsConverter.convert(invoice.getInvoice()))
                .externalID(refund.getExternalId())
                .reason(refund.getReason())
                .transactionInfo(transactionInfoConverter.convert(transactionInfo))
                .error(status.isSetFailed() ? ErrorUtils.getError(status.getFailed().getFailure()) : null);
    }

    private TransactionInfo getTransactionInfo(List<InvoiceRefundSession> sessions) {
        return sessions.size() > 0 ? sessions.get(sessions.size() - 1).getTransactionInfo() : null;
    }

}
