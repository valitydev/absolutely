package com.rbkmoney.absolutely.converter;

import com.rbkmoney.absolutely.exception.NotFoundException;
import com.rbkmoney.absolutely.utils.InvoiceUtils;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.absolutely.utils.CashFlowUtils;
import com.rbkmoney.absolutely.utils.ErrorUtils;
import com.rbkmoney.absolutely.utils.TimeUtils;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentSession;
import com.rbkmoney.swag.adapter.abs.model.*;
import com.rbkmoney.swag.adapter.abs.model.PaymentRoute;
import com.rbkmoney.swag.adapter.abs.model.RiskScore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentConverter {

    private final PaymentPayerConverter paymentPayerConverter;
    private final InvoiceDetailsConverter invoiceDetailsConverter;
    private final TransactionInfoConverter transactionInfoConverter;

    public Payment convert(com.rbkmoney.damsel.payment_processing.Invoice invoice, String paymentId) {
        InvoicePayment invoicePayment = InvoiceUtils.extractPayment(invoice, paymentId);
        var payment = invoicePayment.getPayment();
        TransactionInfo transactionInfo = getTransactionInfo(invoicePayment.getSessions());
        InvoicePaymentStatus status = payment.getStatus();
        var fees = invoicePayment.getCashFlow() != null ? CashFlowUtils.getFees(invoicePayment.getCashFlow()) : null;

        return new Payment()
                .id(payment.getId())
                .invoiceID(invoice.getInvoice().getId())
                .createdAt(TimeUtils.toOffsetDateTime(payment.getCreatedAt()))
                .domainRevision(payment.getDomainRevision())
                .partyRevision(payment.getPartyRevision())
                .partyID(payment.getOwnerId())
                .shopID(payment.getShopId())
                .flow(convertPaymentFlow(payment.getFlow()))
                .status(Payment.StatusEnum.fromValue(status.getSetField().getFieldName()))
                .amount(payment.getCost().getAmount())
                .currency(payment.getCost().getCurrency().getSymbolicCode())
                .fee(CashFlowUtils.getFee(fees))
                .providerFee(CashFlowUtils.getProviderFee(fees))
                .externalFee(CashFlowUtils.getExternalFee(fees))
                .invoiceDetails(invoiceDetailsConverter.convert(invoice.getInvoice()))
                .payer(paymentPayerConverter.convert(payment.getPayer()))
                .makeRecurrent(payment.isSetMakeRecurrent() ? payment.isMakeRecurrent() : null)
                .externalID(payment.getExternalId())
                .riskScore(convertRiskScore())
                .paymentRoute(convertPaymentRoute(invoicePayment.getRoute()))
                .transactionInfo(transactionInfoConverter.convert(transactionInfo))
                .error(status.isSetFailed() ? ErrorUtils.getError(status.getFailed().getFailure()) : null);
    }

    private PaymentFlow convertPaymentFlow(InvoicePaymentFlow paymentflow) {
        if (paymentflow.isSetHold()) {
            return new PaymentFlowHold()
                    .heldUntil(TimeUtils.toOffsetDateTime(paymentflow.getHold().getHeldUntil()))
                    .onHoldExpiration(PaymentFlowHold.OnHoldExpirationEnum.fromValue(paymentflow.getHold().getOnHoldExpiration().name()));
        } else if (paymentflow.isSetInstant()) {
            return new PaymentFlowInstant();
        } else {
            throw new IllegalStateException("Unknown flow type "+ paymentflow.getSetField().getFieldName());
        }
    }

    private TransactionInfo getTransactionInfo(List<InvoicePaymentSession> sessions) {
        return sessions.size() > 0 ? sessions.get(sessions.size() - 1).getTransactionInfo() : null;
    }

    private PaymentRoute convertPaymentRoute(com.rbkmoney.damsel.domain.PaymentRoute route) {
        return new PaymentRoute()
                .providerID(route.getProvider().getId())
                .terminalID(route.getTerminal().getId());
    }

    private RiskScore convertRiskScore() {
        return null; //TODO
    }
}
